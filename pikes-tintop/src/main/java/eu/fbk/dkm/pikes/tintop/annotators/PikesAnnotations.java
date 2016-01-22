package eu.fbk.dkm.pikes.tintop.annotators;

import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.dkm.pikes.tintop.annotators.raw.LinkingTag;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Word;

import java.util.List;

/**
 * Created by alessio on 27/05/15.
 */

public class PikesAnnotations {

    private PikesAnnotations() {

    }

    public static final String PIKES_SIMPLEPOS = "simple_pos";
    public static final Annotator.Requirement SIMPLEPOS_REQUIREMENT = new Annotator.Requirement(PIKES_SIMPLEPOS);

    public static final String PIKES_WORDNET = "wordnet";
    public static final Annotator.Requirement WORDNET_REQUIREMENT = new Annotator.Requirement(PIKES_WORDNET);

    public static final String PIKES_CONLLPARSE = "conll_parse";
    public static final Annotator.Requirement CONLLPARSE_REQUIREMENT = new Annotator.Requirement(PIKES_CONLLPARSE);

    public static final String PIKES_MSTPARSE = "mst_parse";
    public static final Annotator.Requirement MSTPARSE_REQUIREMENT = new Annotator.Requirement(PIKES_MSTPARSE);

    public static final String PIKES_SRL = "srl";
    public static final Annotator.Requirement SRL_REQUIREMENT = new Annotator.Requirement(PIKES_SRL);

    public static final String PIKES_SEMAFOR = "semafor";
    public static final Annotator.Requirement SEMAFOR_REQUIREMENT = new Annotator.Requirement(PIKES_SEMAFOR);

    public static final String PIKES_DBPS = "dbps";
    public static final Annotator.Requirement DBPS_REQUIREMENT = new Annotator.Requirement(PIKES_DBPS);

    public static final String PIKES_LINKING = "linking";
    public static final Annotator.Requirement LINKING_REQUIREMENT = new Annotator.Requirement(PIKES_LINKING);

    public static class MstParserAnnotation implements CoreAnnotation<DepParseInfo> {

        @Override public Class<DepParseInfo> getType() {
            return DepParseInfo.class;
        }
    }

    public static class UKBAnnotation implements CoreAnnotation<String> {

        @Override public Class<String> getType() {
            return String.class;
        }
    }

    public static class SimplePosAnnotation implements CoreAnnotation<String> {

        @Override public Class<String> getType() {
            return String.class;
        }
    }

    public static class DBpediaSpotlightAnnotation implements CoreAnnotation<LinkingTag> {

        @Override public Class<LinkingTag> getType() {
            return LinkingTag.class;
        }
    }

    public static class LinkingAnnotations implements CoreAnnotation<List<LinkingTag>> {

        @Override public Class<List<LinkingTag>> getType() {
            return ErasureUtils.<Class<List<LinkingTag>>>uncheckedCast(List.class);
        }
    }

    public static class SemaforAnnotation implements CoreAnnotation<SemaforParseResult> {

        @Override public Class<SemaforParseResult> getType() {
            return SemaforParseResult.class;
        }
    }

    public static class MateAnnotation implements CoreAnnotation<Predicate> {

        @Override public Class<Predicate> getType() {
            return Predicate.class;
        }
    }

    public static class MateTokenAnnotation implements CoreAnnotation<Word> {

        @Override public Class<Word> getType() {
            return Word.class;
        }
    }

//	public static class ConllParserAnnotation implements CoreAnnotation<DepPair> {
//		public Class<DepPair> getType() {
//			return DepPair.class;
//		}
//	}

}