package com.neo4j.CSV2Cypher.swagger;

import java.util.*;
import java.util.stream.Collectors;

public class ApiDocs {
    public String swagger = "2.0";
    public Info info = new Info();
    public String host = "localhost:9091";
    public String basePath = "/";
    public List<Tag> tags = new ArrayList<>();
    public Map<String,Object> paths = new HashMap<>();
    public Map<String,Object> definitions = new HashMap<>();

    public class Info{
		public String description = "Api Documentation";
        public String version = "1.0";
        public String title = "Api Documentation";
        public String termsOfService = "urn:tos";
        public Object contact = new Object();
        public License license = new License();

        public class License {
            public String name = "Apache 2.0";
            public String url = "http://www.apache.org/licenses/LICENSE-2.0";
        }
    }

    public class Tag{
        public String name;
        public String description;

        public Tag(String name){
            this.name=name;
            this.description="Operations pertaining to " + name;
        }
    }

    public class Method{
        public String[] tags;
        public String summary;
        public String description;
        public String operationId;
        public String[] consumes = new String[] {"application/json"};
        public String[] produces = new String[] {"*/*"};
        public List<Parameter> parameters = new ArrayList<>();
        public Map<String,Object> responses = new HashMap<>();
        public boolean deprecated = false;

        public class Parameter{
            public String in = "body";
            public String name;
            public String description;
            public boolean required = false;
            public Schema schema;
            public String type;

            public Parameter(String label, String in, boolean required, boolean isArray){
                String properCase = Character.toLowerCase(label.charAt(0)) + label.substring(1);
                this.in = in;
                this.name = properCase;
                this.description = properCase;
                this.required = required;
                if (in.equals("path")){
                    this.type="string";
                }
                else {
                    this.schema = new Schema(label, isArray);
                }
            }
        }

        public class Response{
            public String description;
            public Schema schema;

            public Response(String desc, Schema schema){
                this.description = desc;
                this.schema = schema;
            }
        }

        public void addParameter(String label, String in, boolean required, boolean isArray){
            this.parameters.add(new Parameter(label,in,required,isArray));
        }

        public void addResponses(String label, String desc, boolean returnsObject, boolean isArray){
            this.responses.put("200",new Response(desc , returnsObject ? new Schema(label,isArray) : null));
            this.responses.put("401",new Response( "Unauthorized", null));
            this.responses.put("403",new Response( "Forbidden", null));
            this.responses.put("404",new Response( "Not Found", null));

        }
    }

    public class Schema{
        public String $ref;
        public String type;
        public Schema items;

        public Schema (String label, boolean isArray){
            if ( isArray){
                this.type = "array";
                this.items = new Schema(label,false);
            }
            else{
                this.$ref = "#/definitions/"+label;
            }
        }
    }

    public class Definition{
        public String type = "object";
        public Map<String,Object> properties = new HashMap<>();

        public class Property{
            public String type = "string";
        }

        public void addProperty (String field){
            properties.put(field, new Property());
        }
    }

    public void addTag(String name){
        tags.add(new Tag(name + "-controller"));
    }

    public void addPaths(String label,String javaParent,int id){
        String path = "/api/bom" +Arrays.stream(javaParent.split("\\."))
                .map(s-> {
                    if (s.length() > 0) {
                        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
                    } else {
                        return s;
                    }
                })
                .collect(Collectors.joining("/")) +
                "/" + Character.toLowerCase(label.charAt(0))+label.substring(1);
        HashMap<String,Object> posts = new HashMap<>();
        //create Post method
        Method method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Matches for the specified %s node(s) based on the provided criteria.",label);
        method.description = " Use \\|\\| as a delimiter for multiple values ";
        method.operationId = "searchUsingPOST_" + id;
        method.addParameter(label,"body",false,false);
        method.addResponses(label, label + " search successfully executes", true,true);
        posts.put("post",method);

        //create Put method
        method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Creates a new %s node only if one does not already exist with the specified base.",label);
        method.operationId = "createUsingPUT_" + id;
        method.addParameter(label,"body",true,false);
        method.addResponses(label, label + " node is successfully created", true,false);
        posts.put("put",method);

        //create Delete method
        method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Matches for the specified %s node and deletes it.",label);
        method.operationId = "deleteUsingDELETE_" + id;
        method.addParameter(label,"body",true,false);
        method.addResponses(label, label + " node is successfully deleted", false,false);
        posts.put("delete",method);

        //create Patch method
        method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Matches for the specified %s node. If one is found, then update the specified properties.",label);
        method.operationId = "updateUsingPATCH_" + id;
        method.addParameter(label,"body",true,false);
        method.addResponses(label, label + " node is successfully updated", true,false);
        posts.put("patch",method);

        this.paths.put(path,posts);

        posts = new HashMap<>();
        String ftpath = path + "/ftsearch/{term}";

        //create Post FT Search method
        method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Matches for the specified %s node(s) based on the provided criteria.",label);
        method.operationId = "fullTextSearchUsingGET_" + id;
        method.addParameter("term","path",true,false);
        method.addResponses(label, label + " search successfully executes", true,true);
        posts.put("post",method);

        this.paths.put(ftpath,posts);

        posts = new HashMap<>();
        String getPath = path + "/{id}";
        //create Post FT Search method
        method = new Method();
        method.tags = new String[]{label +"-controller"};
        method.summary = String.format("Matches for the specified %s node.",label);
        method.operationId = "getUsingGET_" + id;
        method.consumes = null;
        method.addParameter("id","path",true,false);
        method.addResponses(label, "Ok", true,false);
        posts.put("get",method);

        this.paths.put(getPath,posts);

    }

    public void addDefinition(String label, List<String> fields){
        Definition def = new Definition();
        for (String field : fields){
            def.addProperty(field);
        }
        this.definitions.put(label,def);
    }
}

