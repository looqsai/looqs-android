#!/bin/bash

# Define the output file
output_file="extracted_files_contents.txt"

# Empty the output file if it already exists
> "$output_file"

# Recursively find files with the specified extensions and append their paths and contents to the output file
find . -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.xml" \) | while read file; do
  # Write the relative file path and name to the output file
  echo "File: $file" >> "$output_file"
  echo "----------------------------------------" >> "$output_file"

  # Write the file contents to the output file
  cat "$file" >> "$output_file"

  # Separate each file's contents in the output file
  echo -e "\n\n" >> "$output_file"
done

echo "Extraction complete. Contents saved in $output_file."
