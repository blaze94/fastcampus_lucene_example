package fastcampus.lucene.example.analyzer;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;


public class JasoTokenFilterFactory extends TokenFilterFactory {

  /**
   * Initialize this factory via a set of key-value pairs.
   */
  private boolean isQuery = true;
  private int mode = 0;
  private boolean typo = false;

  public JasoTokenFilterFactory(Map<String, String> args) {
    super(args);
    this.isQuery = getBoolean(args, "isQuery", true);
    this.mode = getInt(args, "mode", 0);
    this.typo = getBoolean(args, "typo", false);
  }

  public TokenStream create(TokenStream tokenstream) {
    return new JasoTokenFilter(tokenstream,this.mode, this.typo);
  }
}
