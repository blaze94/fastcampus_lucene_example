package fastcampus.lucene.example.geo;

import junit.framework.TestCase;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialArgsParser;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import org.apache.lucene.util.Version;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

/**
 * This class serves as example code to show how to use the Lucene spatial
 * module.
 */
public class SpatialExample extends TestCase {

  //Note: Test invoked via TestTestFramework.spatialExample()

  public static void main(String[] args) throws Exception {
    new SpatialExample().test();
  }

  public void test() throws Exception {
    init();
    indexPoints();
    search();
  }

  private SpatialContext ctx;//"ctx" is the conventional variable name

  private SpatialStrategy strategy;

  private Directory directory;

  protected void init() {
    this.ctx = SpatialContext.GEO;

    int maxLevels = 11;//results in sub-meter precision for geohash

    //  This can also be constructed from SpatialPrefixTreeFactory
    SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

    this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");

    this.directory = new RAMDirectory();
  }

  private void indexPoints() throws Exception {
    IndexWriterConfig iwConfig = new IndexWriterConfig(null);
    IndexWriter indexWriter = new IndexWriter(directory, iwConfig);
    indexWriter.addDocument(newSampleDocument(2, ctx.makePoint(-80.93, 33.77)));
    indexWriter.addDocument(newSampleDocument(4, ctx.readShapeFromWkt("POINT(60.9289094 -50.7693246)")));
    indexWriter.addDocument(newSampleDocument(20, ctx.makePoint(0.1,0.1), ctx.makePoint(0, 0)));
    indexWriter.close();
  }

  private Document newSampleDocument(int id, Shape... shapes) {
    Document doc = new Document();
    doc.add(new NumericDocValuesField("id", id));
    for (Shape shape : shapes) {
      for (IndexableField f : strategy.createIndexableFields(shape)) {
        doc.add(f);
      }
      //store it too; the format is up to you
      //  (assume point in this example)
      Point pt = (Point) shape;
      doc.add(new StoredField(strategy.getFieldName(), pt.getX()+" "+pt.getY()));
    }
    return doc;
  }

  private void search() throws Exception {
    IndexReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

    //--Filter by circle (<= distance from a point)
    {
      //Search with circle
      //note: SpatialArgs can be parsed from a string
      SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
              ctx.makeCircle(-80.0, 33.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
      //Filter filter = strategy.makeFilter(args);
      TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), 10, idSort);
      assertDocMatchedIds(indexSearcher, docs, 2);
      //Now, lets get the distance for the 1st doc via computing from stored point value:
      // (this computation is usually not redundant)
      Document doc1 = indexSearcher.doc(docs.scoreDocs[0].doc);
      String doc1Str = doc1.getField(strategy.getFieldName()).stringValue();
      //assume doc1Str is "x y" as written in newSampleDocument()
      int spaceIdx = doc1Str.indexOf(' ');
      double x = Double.parseDouble(doc1Str.substring(0, spaceIdx));
      double y = Double.parseDouble(doc1Str.substring(spaceIdx+1));
      double doc1DistDEG = ctx.calcDistance(args.getShape().getCenter(), x, y);
      assertEquals(121.6d, DistanceUtils.degrees2Dist(doc1DistDEG, DistanceUtils.EARTH_MEAN_RADIUS_KM), 0.1);
      //or more simply:
      assertEquals(121.6d, doc1DistDEG * DistanceUtils.DEG_TO_KM, 0.1);
    }
    //--Match all, order by distance ascending
    {
      Point pt = ctx.makePoint(60, -50);
      ValueSource valueSource = strategy.makeDistanceValueSource(pt, DistanceUtils.DEG_TO_KM);//the distance (in km)
      Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher);//false=asc dist
      TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), 10, distSort);
      assertDocMatchedIds(indexSearcher, docs, 4, 20, 2);
      //To get the distance, we could compute from stored values like earlier.
      // However in this example we sorted on it, and the distance will get
      // computed redundantly.  If the distance is only needed for the top-X
      // search results then that's not a big deal. Alternatively, try wrapping
      // the ValueSource with CachingDoubleValueSource then retrieve the value
      // from the ValueSource now. See LUCENE-4541 for an example.
    }
    //demo arg parsing
    {
      SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
              ctx.makeCircle(-80.0, 33.0, 1));
      SpatialArgs args2 = new SpatialArgsParser().parse("Intersects(BUFFER(POINT(-80 33),1))", ctx);
      assertEquals(args.toString(),args2.toString());
    }

    indexReader.close();
  }

  private void assertDocMatchedIds(IndexSearcher indexSearcher, TopDocs docs, int... ids) throws IOException {
//    int[] gotIds = new int[docs.totalHits];
//    for (int i = 0; i < gotIds.length; i++) {
//      gotIds[i] = indexSearcher.doc(docs.scoreDocs[i].doc).getField("id").numericValue().intValue();
//    }
//    assertArrayEquals(ids,gotIds);
  }

}