package tiny;

import tiny.lexer.Lexer;
import tiny.lexer.Lexer.State;
import tiny.node.*;

//Please note, some of this code was taken and or inspired by
//http://www.sable.mcgill.ca/~hendren/520/2016/semicolon-test/
//All rights and original authors can be seen on the page cited above.

public class GoLexer extends Lexer {
        private Token last_token = null;

        public GoLexer(java.io.PushbackReader in) {
                super(in);
        }

        private boolean requires_semicolon() {
                return
		    ( token instanceof TEol &&
		    (last_token instanceof TIdentifier
			|| last_token instanceof TInteger
      || last_token instanceof TFloat
      || last_token instanceof TRune
      || last_token instanceof TString
      || last_token instanceof TBreak
      || last_token instanceof TContinue
      || last_token instanceof TReturn
      || last_token instanceof TFallthrough
      || last_token instanceof TRPar
      || last_token instanceof TSquareBracketR
      || last_token instanceof TBracketR
      || last_token instanceof TPlusPlus
      || last_token instanceof TMinusMinus
      //|| last_token instanceof TBlank
      //|| last_token instanceof TComment
      ));
        }

        protected void filter() {
                if (requires_semicolon())
                        token = new TSemicolon();
                last_token = token;
        }
}
