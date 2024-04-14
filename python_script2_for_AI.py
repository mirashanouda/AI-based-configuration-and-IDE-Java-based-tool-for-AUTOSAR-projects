# Define the file paths
input_file_path = 'user_input.txt'
output_file_path = 'outputAI.txt'

# Open and read the input file
with open(input_file_path, 'r') as input_file:
    # Read all lines in the file
    lines = input_file.readlines()
    
    # Check if there are at least 1 line
    if len(lines) >= 1:
        # Get the first line
        first_line = lines[0]
        
        # Open and write the first line to the output file
        with open(output_file_path, 'w') as output_file:
            output_file.write(first_line)
    else:
        print("The input file does not have at least 1 line.")
