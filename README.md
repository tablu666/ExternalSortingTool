# External Sorting Tool

#### A tool based on Java that can sort binary data files in a limited working memory space.
This is an external sorting tool that takes a binary data file (contains records with ID and value) as an input, and outputs a file sorting the values in ascending order.

## Input file
- Size of n (>1) blocks.

- A **block** is 8K (8,192) bytes, which contain 512 records.

- A **record** is 16 bytes long and contains two fields.
	> The first 8-byte field is a non-negative integer value of type `long` for the record ID and the second 8-byte field is the record value of type `double`, which will be used for sorting.

## Working memory
- Size of 10 blocks and additional working variables.
- 1 block of input buffer.
- 1 block of output buffer.
- 8 blocks of **heap** in size.

## Run file
- Size of n run(s).
- A **run** is at least 8 blocks long, which contains the sorted data.

## Output file
- Size of n (>1) blocks.
- Contains record sorted by value in ascending order. 

## Algorithms
#### Replacement selection with min-heap
To process the data, read the first 8 blocks of the input file into working memory and use replacement selection to create the **longest possible run**. As it is being created, the run is output to the one block output buffer. Whenever this output buffer becomes full, it is written to an output file called the **run file**. When the first run is complete, continue on to the next section of the input file, adding the second run to the end of the run file. When the process of creating runs is complete, the run file will contain some number of runs, each run being at least 8 blocks long, with the data sorted within each run.

#### Multi-way merge
Multi-way merging uses the **8 blocks of memory** used for the heap in the run-building step to store working data from the runs during the merge step. Multi-way merging is done by reading the first block from each of the runs currently being merged into your working area, and merging these runs into the one block output buffer. When the output buffer fills up, it is written to the output file. Whenever one of the input blocks is exhausted, read in the next block for that particular run. This step requires random access (using `seek()`) to the run file, and a sequential write of the output file. Depending on the size of all records, it may need **multiple passes** of multiway-merging to sort the whole file.

## Classes
1. `Externalsort` is then name of the program. It contains the main method (the entry point of the application).
2. `GenFile` will generate input files with randomized data for you to use in your testing.
3. `IOHelper` provides useful binary data read and write methods.
4. `MaxHeap` is used when the size of the input file is less than 8 blocks long.
5. `MinHeap` is used when it comes to replacement selection.
6. `Operator` is responsible for two phases operations. (Replacement selection & Multi-way merge).
7. `Record`represents the record of the input file.
8. `RunInfo` stores the start position and the length of a run.

## How to run
1. The `GenFile` can be invoked from the command-line as: 
```console
%> java GenFile <filename> <size-in-blocks>
```
2. The program will be invoked from the command-line as:
```console
%> java Externalsort <record-filename>
```