def convert_tabs_to_spaces(input_file_path, output_file_path):
    # Define the replacement: each tab to be replaced with two spaces
    tab_replacement = '    '
    
    try:
        # Open the input file for reading
        with open(input_file_path, 'r') as file:
            # Read the contents of the file
            file_contents = file.read()
        
        # Replace all instances of the tab character with two spaces
        modified_contents = file_contents.replace('\t', tab_replacement)
        
        # Open the output file for writing (will create the file if it doesn't exist)
        with open(output_file_path, 'w') as file:
            # Write the modified contents to the file
            file.write(modified_contents)
        
        print(f"File '{input_file_path}' has been successfully processed. Tabs have been converted to spaces in '{output_file_path}'.")
    except IOError as e:
        # Handle potential file input/output errors
        print(f"An error occurred while processing the file: {e}")