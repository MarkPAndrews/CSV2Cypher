package com.neo4j.CSV2Cypher;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.neo4j.CSV2Cypher.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class Util {
    static Logger logger = LoggerFactory.getLogger(Util.class);
    static ObjectMapper mapper = new ObjectMapper();


    public static List<String> convertResultSetToList(ResultSet resultSet) throws SQLException, JsonProcessingException {
        StringJoiner commaJoiner = new StringJoiner(", \n");

        JsonNode node = mapper.createObjectNode();

        List<String> rowList = new ArrayList();

        while (resultSet.next()) {
            Map<String, String> rowMap = new HashMap();

            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                String columnValue = "\"" + resultSet.getString(i) + "\"";
                rowMap.put(columnName, columnValue);
            }
            // Convert the map to JSON
            String row = mapper.writeValueAsString(rowMap);

            rowList.add(row);
        }
        return rowList;
    }

    /**
     * Load the valus from a properties file stored in the classpath
     *
     * @param propFile - Name of the file to load
     * @return - The list of Properties
     * @throws IOException
     */
    static Properties loadProperties(String propFile) throws IOException {
        Properties props = new Properties();
        props.load(Util.class.getClassLoader().getResourceAsStream(propFile));
        return props;
    }

    static Configuration loadConfiguration (String fileName){
        Gson gson = new Gson();
        return gson.fromJson(readFile(fileName),Configuration.class);
    }

    /**
     * Convert a string containing underscores (FIRST_NAME or first_name) to camel case (firstName).
     *
     * @param text - The input string with underscores
     * @return - The input string with underscores removed converted to camle case (fisrtName).
     */
    static String string2CamelCase(String text) {
        if (text==null ||!text.contains("_")) return text;
        Matcher m = Pattern.compile("([_][a-z])").matcher(text.toLowerCase());
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group().substring(1).toUpperCase());
        }
        m.appendTail(sb);

        return sb.toString();
    }

    static String readFile(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    static String quotedOrNull (String value){
        if (value==null){
            return value;
        }
        else{
            return String.format("\"%s\"",value);
        }

    }
}
