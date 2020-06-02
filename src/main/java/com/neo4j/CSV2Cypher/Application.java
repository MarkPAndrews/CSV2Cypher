package com.neo4j.CSV2Cypher;

import com.google.gson.Gson;
import com.neo4j.CSV2Cypher.config.Configuration;
import com.neo4j.CSV2Cypher.config.Mapping;
import com.neo4j.CSV2Cypher.config.Relationship;
import com.neo4j.CSV2Cypher.swagger.ApiDocs;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SpringBootApplication
public class Application implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(getClass());
    private String cypherTemplate;
    private String javaClassTemplate;
    private String javaPropertyTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            String inputFile = args[0];
            generateFiles(inputFile);
            return;
        }
        throw new InvalidParameterException("\nInvalid parameters.\n PARAMETERS: <path to mapper.json>");

    }

    private void generateFiles(String fileName) {
        // Load the mapping configuration
        Configuration config = Util.loadConfiguration(fileName);
        logger.warn("Found {} object in {}", config.getMappings().length, fileName);
        StringBuilder indexFile = new StringBuilder();
        StringBuilder nodesFile = new StringBuilder();
        StringBuilder relationshipFile = new StringBuilder();
        StringBuilder nodesResyncFile = new StringBuilder();
        StringBuilder relationshipResyncFile = new StringBuilder();
        ApiDocs swagger = new ApiDocs();

        // Loop through the properties converting each csv
        int i = 0;
        for (Mapping csvObject : config.getMappings()) {
            try {
                if (convertObject(csvObject, indexFile, nodesFile, relationshipFile, nodesResyncFile, relationshipResyncFile, swagger, config, i)) i++;

            } catch (Exception e) {
                logger.error("Exception in Application run", e);
            }
        }

        writeFile("indexes.cypher", indexFile.toString(), config.getCypher().getOutputDir());
        writeFile("nodes.cypher", nodesFile.toString(), config.getCypher().getOutputDir());
        writeFile("relationships.cypher", relationshipFile.toString(), config.getCypher().getOutputDir());
        writeFile("nodesResync.cypher", nodesResyncFile.toString(), config.getCypher().getOutputDir());
        writeFile("relationshipsResync.cypher", relationshipResyncFile.toString(), config.getCypher().getOutputDir());

        Gson gson = new Gson();
        writeFile("api-docs.json", gson.toJson(swagger), config.getCypher().getOutputDir());

        logger.warn("processed {} objects out of {}", i, config.getMappings().length);

    }

    private static String column2Cypher(String columnName, List<String> lowerList, List<String> dateFields) {

        String property = Util.string2CamelCase(columnName);
        String template = "set n.%s = line.%s";
        if (lowerList.contains(columnName)) {
            template = "set n.%s = toLower(line.%s)";
        } else if (dateFields.contains(columnName)) {
            template = "set n.%s = line.%s+'T12:00:00.0Z'";
        }

        return String.format(template, property, columnName);
    }

    private static String getJavaRelationship(Relationship rel) {
        String pattern = "\t\tthis.addRelationship( %s,%s,%s,%s,%s,%s,%s,%s);\n";
        return String.format(pattern, Util.quotedOrNull(rel.getParent()), Util.quotedOrNull(rel.getLabel()),
                Util.quotedOrNull(Util.string2CamelCase(rel.getIdColumn())),
                Util.quotedOrNull(Util.string2CamelCase(rel.getMatchProperty())),
                Util.string2CamelCase(rel.getIdColumn()), Util.quotedOrNull(rel.getDirection()), rel.isCreateParent()
                , Util.quotedOrNull(rel.getIntermediateChild()));
    }

    private void writeFile(String fileName, String content, String directory) {
        try {
            Path path = Paths.get("./" + directory);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            String outputFile = path.toString() + "/" + fileName.toLowerCase();
            logger.info("Writing to file {}", outputFile);

            Files.write(Paths.get("./" + outputFile), content.getBytes());
        } catch (IOException e) {
            logger.error("Exception in convertObject of csv data", e);
        }
    }

    private static String getRelationship(Mapping csvObject, Relationship rel) {
        StringBuilder sb = new StringBuilder();
        String where = "";
        String id = "";
        String idColumn = Util.string2CamelCase(rel.getIdColumn());
        String matchLabel = rel.getIntermediateChild() == null ? csvObject.getLabel() : rel.getIntermediateChild();

        if (idColumn != null && idColumn.length() > 0) {
            idColumn = idColumn.replace("+", "_");
            String matchProp = Util.string2CamelCase(rel.getMatchProperty());
            if (matchProp == null) matchProp = idColumn;
            id = String.format("{%s:n.%s}", matchProp.replace("+", "_"), idColumn);
            where = String.format("where n.%s IS NOT null ", idColumn);
        }
        String mergePattern = rel.getDirection().equals("In") ? "(%s)<-[r:%s]-(%s)" : "(%s)-[r:%s]->(%s)";
        String parentAction = rel.isCreateParent() ? "Merge" : "Match";
        String merge = String.format(mergePattern, "n", rel.getLabel(), "p");
        String count = String.format(mergePattern, ":" + matchLabel, rel.getLabel(), "");
        sb.append("Call apoc.periodic.iterate(").append(String.format("'Match (n:%s) %s return n',\n", matchLabel,
                where)).append(String.format("'%s (p:%s %s)\n\tMerge %s',\n\t", parentAction, rel.getParent(), id,
                merge)).append("{batchSize:1000}) yield committedOperations as cnt").append(String.format("\nreturn " + "cnt + ' relationships imported for %s %s' as msg;\n", matchLabel, rel.getLabel())).append(String.format("Match %s return count(r) + ' relationships created for %s %s' as msg;\n", count, matchLabel, rel.getLabel()));
        return sb.toString();

    }

    private static String getDeleteRelationship(Mapping csvObject, Relationship rel) {
        StringBuilder sb = new StringBuilder();
        String matchLabel = rel.getIntermediateChild() == null ? csvObject.getLabel() : rel.getIntermediateChild();

        String matchPattern = rel.getDirection().equals("In") ? "(:%s)<-[r:%s]-()" : "(:%s)-[r:%s]->()";
        String match = String.format(matchPattern, matchLabel, rel.getLabel());
        sb.append("Call apoc.periodic.iterate(").append(String.format("'Match %s return r',\n", match))
                .append("'Delete r',")
                .append("{batchSize:1000}) yield committedOperations as cnt")
                .append(String.format("\nreturn " + "cnt + ' relationships deleted for %s %s' as msg;\n", matchLabel, rel.getLabel()));
        return sb.toString();
    }

    private void addIndex(String property, String label, StringBuilder indexes) {
        indexes.append(String.format("Create Index on :%s(%s); \n", label, Util.string2CamelCase(property)));
    }

    public boolean convertObject(Mapping csvObject, StringBuilder indexFile, StringBuilder nodesFile,
                                 StringBuilder relFile, StringBuilder nodesResyncFile, StringBuilder relResyncFile,
                                 ApiDocs swagger, Configuration config,
                                 int id) {

        String csvFile = csvObject.getInputFile() + ".csv";
        String csvPath = config.getImportDir() + csvFile;
        logger.info("reading csv \"{}\"",csvPath);

        List<String> toLower = config.getToLower();
        List<String> dateFields = config.getDateFields();

        if (csvObject.getToLower() != null) {
            toLower.addAll(csvObject.getToLower());
        }
        String idProperty = Util.string2CamelCase(csvObject.getIdColumn());
        String idColumn = "line." + csvObject.getIdColumn();
        //if the idColumn is missing generate a UUID
        if(csvObject.getIdColumn()==null) {
            idColumn = "apoc.create.uuid()";
            idProperty = "id";
        }
        //See if it's a compound Id
        if (idProperty.contains("+")) {
            idProperty = idProperty.replace("+", "_");
            String[] ids = idColumn.split(Pattern.quote("+"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ids.length; i++) {
                if (i == 0) {
                    sb.append("(").append(ids[i]);
                } else {
                    sb.append("+'-'+coalesce(line.").append(ids[i]).append(",'')");
                }
            }
            idColumn = sb.append(")").toString();
        }
        //add the constraint to the index file
        indexFile.append(String.format("Create Constraint on (n:%s) Assert n.%s is Unique;\n", csvObject.getLabel(),
                idProperty));
        StringBuilder indexes = new StringBuilder();
        if (csvObject.getIndexes() != null) {
            for (String index : csvObject.getIndexes()) {
                addIndex(index, csvObject.getLabel(), indexes);
            }
        }

        //add tag and paths to Swagger Doc
        swagger.addTag(csvObject.getLabel());
        swagger.addPaths(csvObject.getLabel(), csvObject.getJavaParent(), id);

        StringBuilder setFields = new StringBuilder();
        StringBuilder setJavaProperties = new StringBuilder();

        ArrayList<String> swaggerFields = new ArrayList<>();

        try {
            // read in header line
            FileReader fileReader = new FileReader(csvPath);
            CSVReader csvReader = new CSVReader(fileReader);

            String[] header = csvReader.readNext();
            if (header.length == 1 && header[0].equals(""))
                header = csvReader.readNext(); //This means there is a blank row at the beginning of the file.
            csvReader.close();
            fileReader.close();

            logger.info("Converting metadata");
            boolean idPropertyFound = false;
            for (String val : header) {
                if (csvObject.getIgnoreFields().contains(val) || config.getIgnoreFields().contains(val)) continue;
                //cypher set fields
                if (!val.equals(csvObject.getIdColumn())) {
                    String line = column2Cypher(val, toLower, dateFields);
                    setFields.append("\t" + line + "\n");
                } else {
                    idPropertyFound = true;
                }
                //java properties
                String property = Util.string2CamelCase(val);
                addJavaProperty(property, getJavaPropertyTemplate(config), setJavaProperties);
                //add to swagger fields
                swaggerFields.add(property);

            }
            //if the id is a combination key it needs to be added to the Java Class
            if (!idPropertyFound) {
                addJavaProperty(idProperty, getJavaPropertyTemplate(config), setJavaProperties);
            }
            //check for additional properties
            if(csvObject.getJavaProperties()!=null){
                for(String prop : csvObject.getJavaProperties()){
                    addJavaProperty(prop, getJavaPropertyTemplate(config), setJavaProperties);
                }
            }

            StringBuilder javaRelationships = new StringBuilder();
            if (csvObject.getRelationships() != null) {
                logger.info("Converting relationships");
                for (Relationship rel : csvObject.getRelationships()) {
                    String line = getRelationship(csvObject, rel);
                    relFile.append(line + "\n");
                    if(!csvObject.isSkipResyncFileGeneration()){
                        relResyncFile.append(getDeleteRelationship(csvObject,rel))
                                .append(line + "\n");
                    }

                    if (rel.isIndexNeeded() && rel.getMatchProperty() != null && rel.getMatchProperty().length() > 0) {
                        //create an index
                        addIndex(rel.getMatchProperty(), rel.getParent(), indexes);
                    }
                    javaRelationships.append(getJavaRelationship(rel));
                }

            }

            //add fields to swagger doc
            swagger.addDefinition(csvObject.getLabel(), swaggerFields);

            indexFile.append(indexes.toString());


            //Write out Cypher File
            String template = getCypherTemplate(config);

            //replace all the tags
            String text =
                    template.replace("${label}", csvObject.getLabel()).replace("${idProperty}", idProperty).replace(
                            "${idColumn}", idColumn).replace("${setFields}", setFields.toString()).replace("${csvFile}",
                            csvFile).replace("${additionalLabels}", csvObject.getAdditionalLabels());

            nodesFile.append(text);
            if(!csvObject.isSkipResyncFileGeneration()){
                nodesResyncFile.append(text);
            }

            String outputFile =
                    config.getCypher().getOutputDir() + "/" + csvObject.getInputFile() + ".cypher".toLowerCase();
            logger.info("Writing to cypher file {}", outputFile);

            if(!csvObject.isSkipJavaFileGeneration()) {
                String checkErrorFunction = "";
                if (csvObject.isCanIgnoreInvalidError()) {
                    checkErrorFunction = "@Transient\n" +
                            "    public boolean shouldRaiseErrorOnInvalid() {\n" +
                            "        return false;\n" +
                            "    }\n\n";
                }

                //write out Java file
                template = getJavaClassTemplate(config);
                text = template.replace("${label}", csvObject.getLabel()).replace("${javaParent}",
                        csvObject.getJavaParent()).replace("${idProperty}", idProperty).replace("${properties}",
                        setJavaProperties.toString()).replace("${additionalLabels}", csvObject.getAdditionalLabels())
                        .replace("${relationships}", javaRelationships.toString())
                        .replace("${shouldRaiseErrorOnInvalid}", checkErrorFunction);

                writeFile(csvObject.getLabel() + ".java".toLowerCase(),text,config.getJava().getOutputDir());
            }
            return true;
        } catch (IOException e) {
            logger.error("Exception in convertObject of csv data", e);
            return false;
        }
    }

    private void addJavaProperty(String property, String template, StringBuilder setJavaProperties) {
        String PCProperty = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        setJavaProperties.append(template.replace("${property}", property).replace("${PCProperty}",
                PCProperty));
    }

    private String getCypherTemplate(Configuration config) {
        if (cypherTemplate == null) cypherTemplate = Util.readFile(config.getCypher().getTemplate());
        return cypherTemplate;
    }

    private String getJavaClassTemplate(Configuration config) {
        if (javaClassTemplate == null) javaClassTemplate = Util.readFile(config.getJava().getClassTemplate());
        return javaClassTemplate;
    }

    private String getJavaPropertyTemplate(Configuration config) {
        if (javaPropertyTemplate == null) javaPropertyTemplate = Util.readFile(config.getJava().getPropertyTemplate());
        return javaPropertyTemplate;
    }
}
