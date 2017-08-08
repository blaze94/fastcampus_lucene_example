package fastcampus.lucene.example.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Created by jihoon on 2017. 8. 8..
 */
public class TestAnalyzer  extends Analyzer{
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream filter = new JasoTokenFilter(tokenizer,0,false);
        //LowerCaseFilter filter = new LowerCaseFilter(Version.LUCENE_44,tokenizer);
        return new TokenStreamComponents(tokenizer, filter);
    }
}
