
# Compiler (Go to Python language)

## Introduction
This compiler was made using Java and SableCC (a parser, also helping to generate the abstract syntax tree AST).
The compiler handles many Go functionnalities (but not all) such as:
* all Go operators
* comments
* package declaration
* variable declaration
* type declaration
* function declaration
* slices, arrays, structs
* if, switch, for ...
* typechecking

## Notes
This project was realised with my teammate Laurence Stolzenberg
We still have some bugs to resolve:
* inequality between structs
* optimized array init
* make the pretty printer 100% functional

## References
* TinyLang example (course website)
* "Writing a compiler with SableCC", John Tortugo, https://johntortugo.wordpress.com/2011/09/01/writing-a-compiler-with-sablecc/“
* SableCC documentation: http://www.sablecc.org/documentation
* “Concrete to Abstract Syntax Transformations with SableCC”, Nat Pryce, http://www.natpryce.com/articles/000531.html
* “C Standard Library Reference”, https://www.tutorialspoint.com/c_standard_library/index.htm
* “How to concat two string in C?”, http://stackoverflow.com/questions/8465006/how-to-concatenate-2-strings-in-c
