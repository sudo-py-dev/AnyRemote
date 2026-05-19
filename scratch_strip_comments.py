import os
import re

def strip_comments(code):
    out = []
    i = 0
    n = len(code)
    state = "NORMAL" # NORMAL, STRING, CHAR, RAW_STRING, SINGLE_COMMENT, MULTI_COMMENT
    
    while i < n:
        # Check raw string first (triple quotes)
        if state == "NORMAL" and code[i:i+3] == '"""':
            out.append('"""')
            i += 3
            state = "RAW_STRING"
            continue
        elif state == "RAW_STRING" and code[i:i+3] == '"""':
            out.append('"""')
            i += 3
            state = "NORMAL"
            continue
            
        if state == "NORMAL":
            if code[i:i+2] == '//':
                state = "SINGLE_COMMENT"
                i += 2
            elif code[i:i+2] == '/*':
                state = "MULTI_COMMENT"
                i += 2
            elif code[i] == '"':
                state = "STRING"
                out.append('"')
                i += 1
            elif code[i] == "'":
                state = "CHAR"
                out.append("'")
                i += 1
            else:
                out.append(code[i])
                i += 1
                
        elif state == "STRING":
            if code[i] == '\\':
                out.append(code[i:i+2])
                i += 2
            elif code[i] == '"':
                state = "NORMAL"
                out.append('"')
                i += 1
            else:
                out.append(code[i])
                i += 1
                
        elif state == "CHAR":
            if code[i] == '\\':
                out.append(code[i:i+2])
                i += 2
            elif code[i] == "'":
                state = "NORMAL"
                out.append("'")
                i += 1
            else:
                out.append(code[i])
                i += 1
                
        elif state == "RAW_STRING":
            out.append(code[i])
            i += 1
            
        elif state == "SINGLE_COMMENT":
            if code[i] == '\n':
                state = "NORMAL"
                out.append('\n')
                i += 1
            else:
                i += 1
                
        elif state == "MULTI_COMMENT":
            if code[i:i+2] == '*/':
                state = "NORMAL"
                i += 2
            else:
                i += 1
                
    # Clean up trailing spaces and return the code
    lines = "".join(out).splitlines()
    cleaned_lines = [line.rstrip() for line in lines]
    return "\n".join(cleaned_lines) + "\n"

def process_file(filepath):
    print(f"Processing {filepath}...")
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
        
    cleaned_content = strip_comments(content)
    
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(cleaned_content)
    print(f"Successfully stripped comments from {os.path.basename(filepath)}")

def main():
    target_dir = "/home/eliezer/Documents/mi/src/main/kotlin"
    for root, _, files in os.walk(target_dir):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                process_file(filepath)

if __name__ == "__main__":
    main()
