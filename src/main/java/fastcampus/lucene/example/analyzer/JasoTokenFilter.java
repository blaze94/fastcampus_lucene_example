package fastcampus.lucene.example.analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nobaksan on 2015. 11. 18..
 */
public class JasoTokenFilter extends TokenFilter {
    /* The constructor for our custom token filter just calls the TokenFilter
     * constructor; that constructor saves the token stream in a variable named
     * this.input.
     */
    private int decomposeMode = 0;
    private boolean typoMode = true;

    public JasoTokenFilter(TokenStream tokenStream, int mode, boolean typo) {
        super(tokenStream);
        /*
        AttributeSource 에 addAttribute가 정의됨
         */
        this.charTermAttr = addAttribute(CharTermAttribute.class);
        this.posIncAttr = addAttribute(PositionIncrementAttribute.class);
        this.offsetAttribute = addAttribute(OffsetAttribute.class);

        this.terms = new LinkedList<char[]>();
        decomposeMode = mode;
        typoMode = typo;
    }

    /* Like the PlusSignTokenizer class, we are going to save the text of the
     * current token in a CharTermAttribute object. In addition, we are going
     * to use a PositionIncrementAttribute object to store the position
     * increment of the token. Lucene uses this latter attribute to determine
     * the position of a token. Given a token stream with "This", "is", "",
     * ”some", and "text", we are going to ensure that "This" is saved at
     * position 1, "is" at position 2, "some" at position 3, and "text" at
     * position 4. Note that we have completely ignored the empty string at
     * what was position 3 in the original stream.
     */
    private CharTermAttribute charTermAttr;
    private PositionIncrementAttribute posIncAttr;
    private OffsetAttribute offsetAttribute;
    private TypeAttribute typeAtt;
    private Queue<char[]> terms;



    /* Like we did in the PlusSignTokenizer class, we need to override the
     * incrementToken() function to save the attributes of the current token.
     * We are going to pass over any tokens that are empty strings and save
     * all others without modifying them. This function should return true if
     * a new token was generated and false if the last token was passed.
     */


    @Override
    public boolean incrementToken() throws IOException {
        int length = 0;
        int start = -1;
        int end = -1;


        if (!terms.isEmpty()) {
            char[] buffer = terms.poll();
            charTermAttr.setEmpty();
            charTermAttr.copyBuffer(buffer, 0, buffer.length);
            posIncAttr.setPositionIncrement(0);
            offsetAttribute.setOffset(0,buffer.length);

            return true;
        }

        if (!input.incrementToken()) {
            return false;
        } else {
            JasoDecomposer jasoDecomposer = new JasoDecomposer();
            String currentTokenInStream = this.input.getAttribute(CharTermAttribute.class).toString().trim();
            String decomposerTokenInStream =  jasoDecomposer.decomposer(currentTokenInStream);
            if(decomposerTokenInStream.contains("★")){
                String[] spl = decomposerTokenInStream.split("★");
                for(String splData : spl){
                    if("".equals(splData.trim())) continue;
                    terms.add(splData.toCharArray());
                }
            }
            if(decomposerTokenInStream.equals("")){
                decomposerTokenInStream = currentTokenInStream;
            }
            if(decomposerTokenInStream.equals(currentTokenInStream)){
                return true;
            }
            return true;
        }
    }

}
