# Define the compiler
CC=gcc

# Define any compile-time flags
CFLAGS=-std=c99 -w -g #-Wall 

# Define the target executable
TARGET=main

# Default target
all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o $(TARGET) $(TARGET).c CanNM.c

# Clean the build
clean:
	rm -rf $(TARGET)
