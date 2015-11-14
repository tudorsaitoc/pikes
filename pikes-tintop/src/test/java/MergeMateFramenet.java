import ch.qos.logback.classic.Level;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.io.Files;
import eu.fbk.dkm.pikes.resources.WordNet;
import eu.fbk.dkm.pikes.resources.util.fnlu.LexUnit;
import eu.fbk.dkm.pikes.resources.util.onsenses.Inventory;
import eu.fbk.dkm.pikes.resources.util.onsenses.Sense;
import eu.fbk.dkm.pikes.resources.util.onsenses.Wn;
import eu.fbk.dkm.pikes.resources.util.propbank.Frameset;
import eu.fbk.dkm.pikes.resources.util.propbank.Predicate;
import eu.fbk.dkm.pikes.resources.util.propbank.Roleset;
import eu.fbk.dkm.pikes.resources.util.semlink.vnfn.SemLinkRoot;
import eu.fbk.dkm.pikes.resources.util.semlink.vnfnroles.Role;
import eu.fbk.dkm.pikes.resources.util.semlink.vnfnroles.SemLinkRolesRoot;
import eu.fbk.dkm.pikes.resources.util.semlink.vnfnroles.Vncls;
import eu.fbk.dkm.pikes.resources.util.semlink.vnpb.Argmap;
import eu.fbk.dkm.pikes.resources.util.semlink.vnpb.PbvnTypemap;
import eu.fbk.dkm.utils.CommandLine;
import net.didion.jwnl.data.PointerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 12/11/15.
 */

public class MergeMateFramenet {

    // Google docs: https://docs.google.com/document/d/1Uexv8352v0eI1Ij1I5j3U9cOHFNKPHlbqCSFTJcFTz4/edit#

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeMateFramenet.class);
    private static HashMap<String, String> lemmaToTransform = new HashMap();
    static final Pattern ONTONOTES_FILENAME_PATTERN = Pattern.compile("(.*)-([a-z]+)\\.xml");
    static final Pattern FRAMEBASE_PATTERN = Pattern
            .compile("^[^\\s]+\\s+[^\\s]+\\s+([^\\s]+)\\s+-\\s+(.+)\\s+-\\s+([a-z])#([0-9]+)$");
    static final Pattern LU_PATTERN = Pattern.compile("^(.*)\\.([a-z]+)$");
    static final Pattern PB_PATTERN = Pattern.compile("^verb-((.*)\\.[0-9]+)$");

    static boolean extract = true;

    public enum OutputMapping {
        PBauto, NBauto, NBresource, PBnofb
    }

    static {
        lemmaToTransform.put("cry+down(e)", "cry+down");
    }

    /**
     * Format the lemma for compatibility between datasets.
     * In particular, spaces and underscores are replaced by '+'
     *
     * @param lemmaFromPredicate the input lemma
     * @return the converted lemma
     */
    protected static String getLemmaFromPredicateName(String lemmaFromPredicate) {
        String lemma = lemmaFromPredicate.replace('_', '+')
                .replace(' ', '+');
        if (lemmaToTransform.keySet().contains(lemma)) {
            lemma = lemmaToTransform.get(lemma);
        }
        return lemma;
    }

    /**
     * Intersect collections of strings, ignoring empty sets
     *
     * @param collections input collection(s)
     * @return the resulting collection (intersection)
     */
    private static Collection<String> getIntersection(Collection<String>... collections) {
        return getIntersection(true, collections);
    }

    /**
     * Intersect collections of strings
     *
     * @param ignoreEmptySets select whether ignoring empty sets for the intersection
     * @param collections     input collection(s)
     * @return the resulting collection (intersection)
     */
    private static Collection<String> getIntersection(boolean ignoreEmptySets, Collection<String>... collections) {
        Collection<String> ret = null;
        for (Collection<String> collection : collections) {
            if (ignoreEmptySets && (collection == null || collection.size() == 0)) {
                continue;
            }
            if (ret == null) {
                ret = new HashSet<>();
                ret.addAll(collection);
            } else {
                ret.retainAll(collection);
            }
        }

        if (ret == null) {
            ret = new HashSet<>();
        }
        return ret;
    }

    private static ArrayList<Matcher> getPropBankPredicates(Roleset roleset) {

        ArrayList<Matcher> ret = new ArrayList<>();

        String source = roleset.getSource();
        if (source != null && source.length() > 0) {

            String[] parts = source.split("\\s+");
            for (String part : parts) {
                if (part.trim().length() == 0) {
                    continue;
                }

                Matcher matcher = PB_PATTERN.matcher(source);
                if (!matcher.find()) {
                    continue;
                }

                ret.add(matcher);
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./merger")
                    .withHeader("Transform linguistic resources into RDF")
                    .withOption("p", "propbank", "PropBank folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("w", "wordnet", "WordNet folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true,
                            false, true)
                    .withOption("o", "ontonotes", "Ontonotes senses folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("l", "lu", "FrameNet LU folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("f", "framebase", "FrameBase FrameNet-WordNet map", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("s", "semlink", "SemLink folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("n", "nombank", "NomBank folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption(null, "ignore-lemma", "ignore lemma information")
                    .withOption("O", "output", "Output file prefix", "PREFIX",
                            CommandLine.Type.STRING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            ((ch.qos.logback.classic.Logger) LOGGER).setLevel(Level.INFO);

            File pbFolder = cmd.getOptionValue("propbank", File.class);
            File nbFolder = cmd.getOptionValue("nombank", File.class);
            File wordnetFolder = cmd.getOptionValue("wordnet", File.class);
            File ontonotesFolder = cmd.getOptionValue("ontonotes", File.class);
            File framebaseFile = cmd.getOptionValue("framebase", File.class);
            File luFolder = cmd.getOptionValue("lu", File.class);
            File semlinkFolder = cmd.getOptionValue("semlink", File.class);

            String outputPattern = cmd.getOptionValue("output", String.class);

            HashMap<OutputMapping, HashMap<String, String>> outputMappingsForPredicates = new HashMap<>();
            HashMap<OutputMapping, HashMap<String, String>> outputMappingsForRoles = new HashMap<>();
            for (OutputMapping outputMapping : OutputMapping.values()) {
                outputMappingsForPredicates.put(outputMapping, new HashMap<>());
                outputMappingsForRoles.put(outputMapping, new HashMap<>());
            }

            boolean ignoreLemmaInFrameBaseMappings = cmd.hasOption("ignore-lemma");

            WordNet.setPath(wordnetFolder.getAbsolutePath());
            WordNet.init();

            JAXBContext fnContext = JAXBContext.newInstance(Frameset.class);
            Unmarshaller fnUnmarshaller = fnContext.createUnmarshaller();

            JAXBContext onContext = JAXBContext.newInstance(Inventory.class);
            Unmarshaller onUnmarshaller = onContext.createUnmarshaller();

            JAXBContext luContext = JAXBContext.newInstance(LexUnit.class);
            Unmarshaller luUnmarshaller = luContext.createUnmarshaller();

            JAXBContext semlinkContext = JAXBContext.newInstance(SemLinkRoot.class);
            Unmarshaller semlinkUnmarshaller = semlinkContext.createUnmarshaller();

            JAXBContext semlinkRolesContext = JAXBContext.newInstance(SemLinkRolesRoot.class);
            Unmarshaller semlinkRolesUnmarshaller = semlinkRolesContext.createUnmarshaller();

            JAXBContext semlinkPbContext = JAXBContext.newInstance(PbvnTypemap.class);
            Unmarshaller semlinkPbUnmarshaller = semlinkPbContext.createUnmarshaller();

            LOGGER.info("Loading SemLink");
            File semlinkFile;

            semlinkFile = new File(semlinkFolder.getAbsolutePath() + File.separator + "vn-pb" + File.separator
                    + "vnpbMappings");
            PbvnTypemap semLinkPb = (PbvnTypemap) semlinkPbUnmarshaller.unmarshal(semlinkFile);

            HashMultimap<String, String> verbnetToPropbank = HashMultimap.create();
            HashMultimap<String, String> propbankToVerbnet = HashMultimap.create();

            for (eu.fbk.dkm.pikes.resources.util.semlink.vnpb.Predicate predicate : semLinkPb.getPredicate()) {
                String lemma = predicate.getLemma();
                Argmap argmap = predicate.getArgmap();
                if (argmap == null) {
                    continue;
                }

                String pbFrame = argmap.getPbRoleset().toLowerCase();
                String vnClass = argmap.getVnClass().toLowerCase();

                verbnetToPropbank.put(vnClass, pbFrame);
                propbankToVerbnet.put(pbFrame, vnClass);

                for (eu.fbk.dkm.pikes.resources.util.semlink.vnpb.Role role : argmap.getRole()) {
                    String pbArg = pbFrame + "@" + role.getPbArg().toLowerCase();
                    String vnTheta = vnClass + "@" + role.getVnTheta().toLowerCase();

                    verbnetToPropbank.put(vnTheta, pbArg);
                    propbankToVerbnet.put(pbArg, vnTheta);
                }

            }

            semlinkFile = new File(semlinkFolder.getAbsolutePath() + File.separator + "vn-fn" + File.separator
                    + "VN-FNRoleMapping.txt");
            SemLinkRolesRoot semLinkRoles = (SemLinkRolesRoot) semlinkRolesUnmarshaller.unmarshal(semlinkFile);

            HashMultimap<String, String> verbnetToFramenet = HashMultimap.create();
            HashMultimap<String, String> framenetToVerbnet = HashMultimap.create();

            for (Vncls vncls : semLinkRoles.getVncls()) {
                String frame = vncls.getFnframe().toLowerCase();
                String vnClass = vncls.getClazz().toLowerCase();

                verbnetToFramenet.put(vnClass, frame);
                framenetToVerbnet.put(frame, vnClass);

                if (vncls.getRoles() == null) {
                    continue;
                }

                for (Role role : vncls.getRoles().getRole()) {
                    String fnRole = frame + "@" + role.getFnrole().toLowerCase();
                    String vnRole = vnClass + "@" + role.getVnrole().toLowerCase();

                    verbnetToFramenet.put(vnRole, fnRole);
                    framenetToVerbnet.put(fnRole, vnRole);
                }
            }

            semlinkFile = new File(
                    semlinkFolder.getAbsolutePath() + File.separator + "vn-fn" + File.separator + "VNC-FNF.s");
            SemLinkRoot semLink = (SemLinkRoot) semlinkUnmarshaller.unmarshal(semlinkFile);

            for (eu.fbk.dkm.pikes.resources.util.semlink.vnfn.Vncls vncls : semLink.getVncls()) {
                String vnClass = vncls.getClazz().toLowerCase();
                String frame = vncls.getFnframe().toLowerCase();

                verbnetToFramenet.put(vnClass, frame);
                framenetToVerbnet.put(frame, vnClass);
            }

            int nbSource = 0;

            LOGGER.info("Loading NomBank files");
            HashMultimap<String, Roleset> nbFrames = HashMultimap.create();
            HashSet<Roleset> nbUnlinked = new HashSet<>();
            for (File file : Files.fileTreeTraverser().preOrderTraversal(nbFolder)) {

                if (!file.isFile()) {
                    continue;
                }

                if (!file.getName().endsWith(".xml")) {
                    continue;
                }

                LOGGER.debug(file.getName());

                Frameset frameset = (Frameset) fnUnmarshaller.unmarshal(file);
                List<Object> noteOrPredicate = frameset.getNoteOrPredicate();
                for (Object predicate : noteOrPredicate) {
                    if (predicate instanceof Predicate) {
                        String lemma = ((Predicate) predicate).getLemma();
                        List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
                        for (Object roleset : noteOrRoleset) {
                            if (roleset instanceof Roleset) {

                                // Warning: this is really BAD!
                                ((Roleset) roleset).setName(lemma);

                                ArrayList<Matcher> predicates = getPropBankPredicates((Roleset) roleset);
                                for (Matcher matcher : predicates) {
                                    String pb = matcher.group(1);
                                    nbFrames.put(pb, (Roleset) roleset);
                                    nbSource++;
                                }

                                if (predicates.size() == 0) {
                                    nbUnlinked.add((Roleset) roleset);
                                }
                            }
                        }
                    }
                }
            }

            LOGGER.info("Loaded {} rolesets with source", nbSource);
            LOGGER.info("Loaded {} frames without source", nbUnlinked.size());

            LOGGER.info("Loading LU files");
            HashMap<String, HashMultimap<String, String>> lus = new HashMap<>();
            HashSet<String> existingFrames = new HashSet<>();

            for (File file : Files.fileTreeTraverser().preOrderTraversal(luFolder)) {
                if (!file.isFile()) {
                    continue;
                }

                if (!file.getName().endsWith(".xml")) {
                    continue;
                }

                LOGGER.debug(file.getName());
                LexUnit lexUnit = (LexUnit) luUnmarshaller.unmarshal(file);
                String lemma = lexUnit.getName();
                existingFrames.add(lexUnit.getFrame());
                Matcher matcher = LU_PATTERN.matcher(lemma);
                if (!matcher.matches()) {
                    LOGGER.error("{} does not match", lemma);
                    continue;
                }

                lemma = matcher.group(1);
                lemma = getLemmaFromPredicateName(lemma);
                String pos = matcher.group(2);
                String frame = lexUnit.getFrame();

                if (lus.get(pos) == null) {
                    lus.put(pos, HashMultimap.create());
                }

                lus.get(pos).put(lemma, frame);
            }

            LOGGER.info("Load FrameBase file");
            HashMultimap<String, String> fbFramenetToWordNet = HashMultimap.create();

            List<String> lines = Files.readLines(framebaseFile, Charsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                Matcher matcher = FRAMEBASE_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }

                String frame = matcher.group(1);
                String lemma = matcher.group(2);
                lemma = getLemmaFromPredicateName(lemma);
                String wnSynset = WordNet.getSynsetID(Long.parseLong(matcher.group(4)), matcher.group(3));

                String key = getFrameBaseKey(frame, lemma, ignoreLemmaInFrameBaseMappings);

                fbFramenetToWordNet.put(key, wnSynset);
            }

//            for (String key : fbFramenetToWordNet.keySet()) {
//                System.out.println(key + " -> " + fbFramenetToWordNet.get(key));
//            }

            int trivialCount = 0;
            int nonTrivialCount = 0;
            int nbCount = 0;
            int emptyRelatedCount = 0;
            int nbGreaterCount = 0;
            int nbZeroCount = 0;
            int unlinkedCount = 0;
            int roleMappingCount = 0;
            int noFrameBaseCount = 0;
            int semlinkCounter = 0;

            if (extract) {
                LOGGER.info("Reading PropBank files");
                for (File file : Files.fileTreeTraverser().preOrderTraversal(pbFolder)) {

                    if (!file.isFile()) {
                        continue;
                    }

                    if (!file.getName().endsWith(".xml")) {
                        continue;
                    }

                    //todo: check ontonotes or not
                    String type;
                    String baseLemma;
                    Matcher matcher = ONTONOTES_FILENAME_PATTERN.matcher(file.getName());
                    if (matcher.matches()) {
                        type = matcher.group(2);
                        baseLemma = matcher.group(1);
                    } else {
                        throw new Exception(
                                "File " + file.getName() + " does not appear to be a good OntoNotes frame file");
                    }

                    if (!type.equals("v")) {
                        continue;
                    }

                    if (!file.getName().equals("add-v.xml")) {
                        continue;
                    }

                    LOGGER.debug(file.getName());

                    HashMap<String, HashMap<String, Set>> senses = getSenses(file.getName(), ontonotesFolder, baseLemma,
                            type, onUnmarshaller);

                    Frameset frameset = (Frameset) fnUnmarshaller.unmarshal(file);
                    List<Object> noteOrPredicate = frameset.getNoteOrPredicate();

                    for (Object predicate : noteOrPredicate) {
                        if (predicate instanceof Predicate) {

                            String lemma = getLemmaFromPredicateName(((Predicate) predicate).getLemma());

                            List<String> synsets = WordNet.getSynsetsForLemma(lemma.replace('+', ' '), type);

                            Set<String> luFrames = lus.get(type).get(lemma);
                            luFrames.retainAll(existingFrames);

                            List<Object> noteOrRoleset = ((Predicate) predicate).getNoteOrRoleset();
                            for (Object roleset : noteOrRoleset) {
                                if (roleset instanceof Roleset) {
                                    String rolesetID = ((Roleset) roleset).getId();
                                    String frameNet = ((Roleset) roleset).getFramnet();

                                    LOGGER.debug(rolesetID);

//                                    for (Object roles : ((Roleset) roleset).getNoteOrRolesOrExample()) {
//                                        if (!(roles instanceof Roles)) {
//                                            continue;
//                                        }
//
//                                        for (Object role : ((Roles) roles).getNoteOrRole()) {
//                                            if (!(role instanceof eu.fbk.dkm.pikes.resources.util.propbank.Role)) {
//                                                continue;
//                                            }
//
//                                            String roleStr = rolesetID + "@"
//                                                    + ((eu.fbk.dkm.pikes.resources.util.propbank.Role) role).getN();
//
//                                            HashSet<String> tempMappingsForRole = new HashSet<>();
//
//                                            for (Vnrole vnrole : ((eu.fbk.dkm.pikes.resources.util.propbank.Role) role)
//                                                    .getVnrole()) {
//                                                String vnClassRole = vnrole.getVncls().toLowerCase();
//                                                String vnThetaRole =
//                                                        vnClassRole + "@" + vnrole.getVntheta().toLowerCase();
//
//                                                Set<String> fnFrames = verbnetToFramenet
//                                                        .get(vnThetaRole);
//                                                tempMappingsForRole.addAll(fnFrames);
//                                            }
//
//                                            if (tempMappingsForRole.size() == 1) {
//                                                for (String frameRole : tempMappingsForRole) {
//                                                    outputMappingsForRoles.get(OutputMapping.PBauto)
//                                                            .put(roleStr, frameRole);
//                                                    roleMappingCount++;
//                                                }
//                                            }
//                                        }
//                                    }

                                    ArrayList<String> fnFrames = new ArrayList<>();
                                    if (frameNet != null) {
                                        String[] fns = frameNet.split("\\s+");
                                        for (String fn : fns) {
                                            if (fn.length() == 0) {
                                                continue;
                                            }
                                            fnFrames.add(fn);
                                        }
                                    }
                                    fnFrames.retainAll(existingFrames);

                                    Collection<String> wnFromSenses = new HashSet<>();
                                    Collection<String> fnFromSenses = new HashSet<>();
                                    if (senses.get(rolesetID) != null) {
                                        wnFromSenses = senses.get(rolesetID).get("wn");
                                        fnFromSenses = senses.get(rolesetID).get("fn");
                                    }
                                    fnFromSenses.retainAll(existingFrames);

                                    System.out.println(synsets);
                                    System.out.println(wnFromSenses);

                                    Collection<String> wnCandidates = getIntersection(synsets, wnFromSenses);

                                    boolean useBaseLemma = false;
                                    String lemmaToUse = lemma;

                                    if (!lemma.equals(baseLemma)) {
                                        if (synsets.size() + wnFromSenses.size() == 0) {
                                            useBaseLemma = true;
                                        }
                                        for (String wnCandidate : wnCandidates) {
                                            Set<String> lemmas = WordNet.getLemmas(wnCandidate);
                                            if (lemmas.contains(baseLemma)) {
                                                useBaseLemma = true;
                                            }
                                        }

                                        if (useBaseLemma && luFrames.size() != 0) {
                                            LOGGER.error("It happens! {}", rolesetID);
                                            useBaseLemma = false;
                                        }
                                    }

                                    Set<String> luFramesToUse = new HashSet<>(luFrames);

                                    if (useBaseLemma) {
                                        LOGGER.debug("Using base lemma");
                                        lemmaToUse = baseLemma;
                                        luFramesToUse = lus.get(type).get(baseLemma);

                                        List<String> newSynsets = WordNet
                                                .getSynsetsForLemma(baseLemma.replace('+', ' '), type);
                                        wnCandidates = getIntersection(wnCandidates, newSynsets);
                                    }

                                    Collection<String> fnCandidates = getIntersection(fnFrames, luFramesToUse,
                                            fnFromSenses);

                                    Collection<String> fnCandidatesOnlySemLink = getIntersection(fnFrames,
                                            fnFromSenses);
                                    if (fnCandidatesOnlySemLink.size() == 1) {
                                        semlinkCounter++;
                                    }

                                    Collection<String> okFrames = getCandidateFrames(wnCandidates, fnCandidates,
                                            lemmaToUse,
                                            type, fbFramenetToWordNet, ignoreLemmaInFrameBaseMappings);

                                    if (rolesetID.equals("add.04")) {
                                        System.out.println(synsets);
                                        System.out.println(wnFromSenses);

                                        System.out.println(fnFrames);
                                        System.out.println(luFramesToUse);
                                        System.out.println(fnFromSenses);

                                        System.out.println(wnCandidates);
                                        System.out.println(fnCandidates);

                                        System.out.println(lemmaToUse);
                                    }

                                    if (fnCandidatesOnlySemLink.size() == 1 && okFrames.size() == 0) {
                                        for (String fnCandidate : fnCandidates) {
                                            outputMappingsForPredicates.get(OutputMapping.PBnofb)
                                                    .put(rolesetID, fnCandidate);
                                            noFrameBaseCount++;
                                        }
                                    }

                                    // If Fp’ contains a singleton frame f, then we align p to f.
                                    // Otherwise we avoid any alignment.
                                    if (okFrames.size() == 1) {
                                        for (String okFrame : okFrames) {
                                            if (fnFrames.size() == 1 && fnFrames.contains(okFrame)) {
                                                trivialCount++;
                                                continue;
                                            }
                                            nonTrivialCount++;

                                            outputMappingsForPredicates.get(OutputMapping.PBauto)
                                                    .put(rolesetID, okFrame);
                                        }
                                    }

                                    // NomBank
                                    Set<Roleset> rolesets = nbFrames.get(rolesetID);
                                    for (Roleset nbRoleset : rolesets) {

                                        // See bad choice above
                                        String nbLemma = nbRoleset.getName();

                                        List<String> nbSynsets = WordNet
                                                .getSynsetsForLemma(nbLemma.replace('+', ' '), "n");

                                        Set<String> relatedSynsets = new HashSet<>();
                                        for (String wnCandidate : wnCandidates) {
                                            relatedSynsets
                                                    .addAll(WordNet.getGenericSet(wnCandidate, PointerType.DERIVED,
                                                            PointerType.NOMINALIZATION, PointerType.PARTICIPLE_OF,
                                                            PointerType.PERTAINYM));
                                        }

                                        if (relatedSynsets.size() == 0) {
                                            emptyRelatedCount++;
                                        }

                                        Set<String> luNbFrames = lus.get("n").get(nbLemma);
                                        Collection<String> fnNbCandidates = getIntersection(fnFrames, luFrames,
                                                fnFromSenses, luNbFrames);

                                        Collection<String> nbCandidates = getIntersection(nbSynsets, relatedSynsets);
                                        Collection<String> okNbFrames = getCandidateFrames(nbCandidates, fnNbCandidates,
                                                nbLemma, "n", fbFramenetToWordNet, ignoreLemmaInFrameBaseMappings);

                                        // If Fp’ contains a singleton frame f, then we align p to f.
                                        // Otherwise we avoid any alignment.
                                        if (okNbFrames.size() == 1) {
                                            for (String okFrame : okNbFrames) {
                                                nbCount++;
                                                outputMappingsForPredicates.get(OutputMapping.NBauto)
                                                        .put(nbRoleset.getId(), okFrame);
                                            }
                                        }
                                        if (okNbFrames.size() > 1) {
                                            nbGreaterCount++;
                                        }
                                        if (okNbFrames.size() == 0) {
                                            nbZeroCount++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Unlinked NomBank
                for (Roleset nbRoleset : nbUnlinked) {

                    // See bad choice above
                    String nbLemma = nbRoleset.getName();
                    List<String> nbSynsets = WordNet.getSynsetsForLemma(nbLemma.replace('+', ' '), "n");

                    if (nbSynsets.size() == 1) {
                        Set<String> frames = lus.get("n").get(nbLemma);
                        if (frames != null && frames.size() == 1) {
                            for (String frame : frames) {
                                outputMappingsForPredicates.get(OutputMapping.NBresource).put(nbRoleset.getId(), frame);
                            }

                            unlinkedCount++;
                        }
                    } else {
                        //todo: check senses
                    }
                }
            }

            // Write files
            BufferedWriter writer;
            File outputFile;

            outputFile = new File(outputPattern + "-frames.tsv");
            LOGGER.info("Writing output file {}", outputFile.getName());
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (OutputMapping outputMapping : outputMappingsForPredicates.keySet()) {
                for (String key : outputMappingsForPredicates.get(outputMapping).keySet()) {
                    String value = outputMappingsForPredicates.get(outputMapping).get(key);

                    writer.append(outputMapping.toString()).append('\t');
                    writer.append(key).append('\t');
                    writer.append(value).append('\n');
                }
            }
            writer.close();

            outputFile = new File(outputPattern + "-roles.tsv");
            LOGGER.info("Writing output file {}", outputFile.getName());
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (OutputMapping outputMapping : outputMappingsForRoles.keySet()) {
                for (String key : outputMappingsForRoles.get(outputMapping).keySet()) {
                    String value = outputMappingsForRoles.get(outputMapping).get(key);

                    writer.append(outputMapping.toString()).append('\t');
                    writer.append(key).append('\t');
                    writer.append(value).append('\n');
                }
            }
            writer.close();

            LOGGER.info("*** STATISTICS ***");

            LOGGER.info("PropBank trivial: {}", trivialCount);
            LOGGER.info("PropBank non-trivial: {}", nonTrivialCount);
            LOGGER.info("PropBank non-FrameBase: {}", noFrameBaseCount);

            LOGGER.info("NomBank (linked): {}", nbCount);
            LOGGER.info("NomBank (unlinked): {}", unlinkedCount);
            LOGGER.info("NomBank (total): {}", unlinkedCount + nbCount);

            LOGGER.info("PropBank (only with SemLink): {}", semlinkCounter);

            LOGGER.info("PropBank roles: {}", roleMappingCount);

            LOGGER.info("No WordNet relations: {}", emptyRelatedCount);
            LOGGER.info("More than one frame: {}", nbGreaterCount);
            LOGGER.info("Zero frames: {}", nbZeroCount);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static HashMap<String, HashMap<String, Set>> getSenses(String name, File ontonotesFolder, String fnLemma,
            String type, Unmarshaller onUnmarshaller)
            throws JAXBException {

        HashMap<String, HashMap<String, Set>> senses = new HashMap<>();

        //todo: add type (for PB 1.7, for example)
        File onSense = new File(ontonotesFolder.getAbsolutePath() + File.separator + name);
        if (onSense.exists()) {

            Inventory inventory = (Inventory) onUnmarshaller.unmarshal(onSense);
            for (Sense sense : inventory.getSense()) {

                if (sense.getMappings() == null) {
                    continue;
                }

                Set<String> onWn = new HashSet<>();
                Set<String> onFn = new HashSet<>();
                Set<String> onPb = new HashSet<>();

                // PropBank
                if (sense.getMappings().getPb() != null) {
                    String[] pbs = sense.getMappings().getPb().split(",");
                    for (String pb : pbs) {
                        pb = pb.trim();
                        if (pb.length() == 0) {
                            continue;
                        }
                        onPb.add(pb);
                    }
                }

                // FrameNet
                if (sense.getMappings().getFn() != null) {
                    String[] fns = sense.getMappings().getFn().split(",");
                    for (String fn : fns) {
                        fn = fn.trim();
                        if (fn.length() == 0) {
                            continue;
                        }
                        onFn.add(fn);
                    }
                }

                // WordNet
                try {
                    for (Wn wn : sense.getMappings().getWn()) {
                        String lemma = wn.getLemma();
                        if (lemma == null || lemma.length() == 0) {
                            lemma = fnLemma;
                        }
                        String value = wn.getvalue();
                        String[] ids = value.split(",");
                        for (String id : ids) {
                            id = id.trim();
                            if (id.length() == 0) {
                                continue;
                            }
                            String synsetID = WordNet.getSynsetID(lemma + "-" + id + type);
                            onWn.add(synsetID);
                        }
                    }
                } catch (Exception e) {
                    // ignored
                }

                for (String pb : onPb) {
                    if (!senses.containsKey(pb)) {
                        senses.put(pb, new HashMap<>());
                    }
                    if (!senses.get(pb).containsKey("wn")) {
                        senses.get(pb).put("wn", new HashSet<>());
                    }
                    if (!senses.get(pb).containsKey("fn")) {
                        senses.get(pb).put("fn", new HashSet<>());
                    }
                    senses.get(pb).get("wn").addAll(onWn);
                    senses.get(pb).get("fn").addAll(onFn);
                }
            }
        }

        return senses;
    }

    private static Collection<String> getCandidateFrames(Collection<String> wnCandidates,
            Collection<String> fnCandidates,
            String lemma, String type, HashMultimap<String, String> fbFramenetToWordNet,
            boolean ignoreLemmaInFrameBaseMappings) {

        Collection<String> okFrames = new HashSet<>();
        lemma = lemma.replace('+', ' ');
        for (String fnCandidate : fnCandidates) {
            String key = getFrameBaseKey(fnCandidate, lemma, type, ignoreLemmaInFrameBaseMappings);
            Collection<String> wnCandidatesForThisFrame = new HashSet<>(fbFramenetToWordNet.get(key));
            wnCandidatesForThisFrame.retainAll(wnCandidates);
            if (wnCandidatesForThisFrame.size() > 0) {
                okFrames.add(fnCandidate);
            }
        }

        return okFrames;
    }

    private static String getFrameBaseKey(String frame, String lemma, String type,
            boolean ignoreLemmaInFrameBaseMappings) {
        return getFrameBaseKey(frame, lemma + "." + type, ignoreLemmaInFrameBaseMappings);
    }

    private static String getFrameBaseKey(String frame, String lemma, boolean ignoreLemmaInFrameBaseMappings) {
        if (ignoreLemmaInFrameBaseMappings) {
            return frame;
        }
        return frame + "-" + lemma;
    }
}