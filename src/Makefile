# assume sablecc (version 3 or later) is on your classpath

# to compile the code
tiny: grammarclean clean compilegrammar
	javac tiny/*.java tiny/lexer/*.java tiny/parser/*.java tiny/node/*.java tiny/analysis/*.java


# to generate the compiler code
grammarclean:
	rm -rf tiny/lexer/
	rm -rf tiny/analysis/
	rm -rf tiny/node/
	rm -rf tiny/parser/
	
	
clean:
	rm -f tiny/*.class tiny/lexer/*.class tiny/parser/*.class tiny/node/*.class tiny/analysis/*.class result
	
compilegrammar: tiny.sablecc
#Add your sablecc to your env so you don't need to specify user specific path
### export PATH=$HOME/sablecc-3.7/bin:$PATH
	sablecc tiny_with_ast.sablecc

check:
	./check
