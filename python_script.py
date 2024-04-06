# Define the file paths
input_file_path = 'output.arxml'
output_file_path = 'IT_WORKED.txt'

# Open and read the input file
with open(input_file_path, 'r') as input_file:
    # Read all lines in the file
    lines = input_file.readlines()
    
    # Check if there are at least two lines
    if len(lines) >= 2:
        # Get the second line
        second_line = lines[1]
        
        # Open and write the second line to the output file
        with open(output_file_path, 'w') as output_file:
            output_file.write(second_line)
    else:
        print("The input file does not have at least two lines.")
