# Define the compiler
CC=gcc

# Define any compile-time flags
CFLAGS=-Wall

# Define the target executable
TARGET=main

# Default target
all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o $(TARGET) $(TARGET).c

# Clean the build
clean:
	rm -rf $(TARGET)
