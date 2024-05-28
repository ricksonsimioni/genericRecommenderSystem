package genericRecommenderSystem.genericRecommenderSystem;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;


import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
	

	
    public static void main(String[] args) {
        try {
        	ResourceSet resourceSetTRS = new ResourceSetImpl();
            resourceSetTRS.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            Resource ecoreResourceTRS = resourceSetTRS.getResource(URI.createFileURI("src/main/Models/recommendersystemGeneric.ecore"), true);
            
            ResourceSet resourceSetDomain = new ResourceSetImpl();
            resourceSetDomain.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            Resource ecoreResourceDomain = resourceSetDomain.getResource(URI.createFileURI("src/main/Models/domain.ecore"), true);
            
            /*ResourceSet resourceSetWeaving = new ResourceSetImpl();
            resourceSetWeaving.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            Resource ecoreResourceWeaving = resourceSetWeaving.getResource(URI.createFileURI("src/main/Models/weaving.ecore"), true);
            */
            EPackage ePackageRS = (EPackage) ecoreResourceTRS.getContents().get(0);
            EPackage ePackageDomain = (EPackage) ecoreResourceDomain.getContents().get(0);
            //EPackage ePackageWeaving = (EPackage) ecoreResourceWeaving.getContents().get(0);

            

            EPackage.Registry.INSTANCE.put(ePackageRS.getNsURI(), ePackageRS);
            EPackage.Registry.INSTANCE.put(ePackageDomain.getNsURI(), ePackageDomain);
            //EPackage.Registry.INSTANCE.put(ePackageWeaving.getNsURI(), ePackageDomain);
            System.out.println("EPackage RS registered: " + EPackage.Registry.INSTANCE.get(ePackageRS.getNsURI()));
            System.out.println("EPackage Domain registered: " + EPackage.Registry.INSTANCE.get(ePackageDomain.getNsURI()));



        	
            // Load the XMI model
            //Resource resource = loadModel("src/main/Models/recommendersystemGeneric.model");

            IEolModule module = new EolModule();
            try {
                // Load the script from file
                String scriptPath = "src/main/Models/EOL_scripts/dataExtraction.eol";
                String scriptContent = new String(Files.readAllBytes(Paths.get(scriptPath)));
                
                // Parse and execute the script
                module.parse(scriptContent);
                
                // Check for parse problems
                if (module.getParseProblems().size() > 0) {
                    System.err.println("Parse problems occurred: " + module.getParseProblems());
                } else {
                    // Execute the script
                    module.execute();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error reading EOL script file: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error executing EOL script: " + e.getMessage());
            }

            String modelURI = "src/main/Models/recommendersystemGeneric.model";
            String metamodelUri = "http://org.rs";
            System.out.println("Model URI: " + modelURI);
            System.out.println("Metamodel URI: " + metamodelUri);


            try {
                List<IModel> models = new ArrayList<>();
                models.add(loadEmfModel("recommendersystemModel", "src/main/Models/recommendersystemGeneric.model", "http://org.rs", true, false));
                models.add(loadEmfModel("domain", "src/main/Models/domain.model", "http://org.rs.domain", true, false));
                
             // Add models to the EOL module
                for (IModel model : models) {
                    module.getContext().getModelRepository().addModel(model);
                }

                // Execute the script
                Object result = module.execute();

                // Handle the result if needed
                System.out.println("Result: " + result);

                // Dispose the models
                for (IModel model : models) {
                    model.dispose();
                }
            
                
                //EmfModel emfModel = createEmfModel("recommendersystemGeneric", modelURI, metamodelUri);
                //module.getContext().getModelRepository().addModel(emfModel);
                //System.out.println(emfModel + modelURI + metamodelUri); 
                //Object result = module.execute();
                //System.out.println(result); 
                //System.out.println("EOL Script executed. Result: " + result);
                //Scanner scanner = new Scanner(System.in);

                
                EEnum toiEnum = (EEnum) ePackageDomain.getEClassifier("Category"); 
                List<String> toiValues = new ArrayList<>();

                if (toiEnum != null && toiEnum.getELiterals() != null) {
                    for (EEnumLiteral literal : toiEnum.getELiterals()) {
                        toiValues.add(literal.getName());
                    }
                }

                    
                if (result instanceof Map) {
                    Map<?, ?> resultMap = (Map<?, ?>) result;
                    
                    // Extract and process ratingsData
                    Object ratingsObj = resultMap.get("ratingsData");
                    FastByIDMap<PreferenceArray> mahoutData = null;
                    if (ratingsObj instanceof List) {
                        mahoutData = processData(extractRatingsData((List<?>) ratingsObj));
                    }

                    // Extract and process itemData
                    Map<Long, Map<String, Object>> itemData = new HashMap<>();
                    if (result instanceof Map) {
                        Object itemDataObj = resultMap.get("itemData");
                        if (itemDataObj instanceof Map) {
                            Map<?, ?> rawItemData = (Map<?, ?>) itemDataObj;
                            rawItemData.forEach((key, value) -> {
                                if (key instanceof Number && value instanceof Map) {
                                    Long itemId = ((Number) key).longValue();
                                    Map<?, ?> details = (Map<?, ?>) value;
                                    Map<String, Object> itemDetails = new HashMap<>();
                                    details.forEach((detailKey, detailValue) -> {
                                        if (detailKey instanceof String) {
                                            itemDetails.put((String) detailKey, detailValue);
                                        }
                                    });
                                    itemData.put(itemId, itemDetails);
                                }
                            });
                    }

                    if (mahoutData != null) {
                        DataModel model = new GenericDataModel(mahoutData);
                        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
                        UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
                        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
                        try (Scanner scanner = new Scanner(System.in)) {
                            System.out.print("Please enter the user ID for which you want recommendations: ");
                            long userId = scanner.nextLong();

                           // System.out.println("What's your favorite topic of interest?" + toiValues);
                          //  String favoriteTOI = scanner.next();

                            List<RecommendedItem> recommendations = recommender.recommend(userId, 10);
                           // List<RecommendedItem> rerankedRecommendations = rerankRecommendations(recommendations, favoriteTOI, itemData);
                            
                           // System.out.println("Recommendations for user ID: " + userId);
                           // for (RecommendedItem recommendation : recommendations) {
                           //     System.out.println(" (ID: " + recommendation.getItemID() + ") - Value: " + recommendation.getValue());
                           // }   
 
                            System.out.println("Recommendations for user ID: " + userId);
                           // for (RecommendedItem recommendation : rerankedRecommendations) {
                            for (RecommendedItem recommendation : recommendations) {
                            	String itemName = (itemData.containsKey(recommendation.getItemID()) && itemData.get(recommendation.getItemID()).containsKey("itemName")) ? itemData.get(recommendation.getItemID()).get("itemName").toString() : "Unknown POI";
                                System.out.println("Recommended POI: " + itemName + " (ID: " + recommendation.getItemID() + ") - Value: " + recommendation.getValue());
                            }
                        }
                    }
                }
               }  

            } catch (EolModelLoadingException e) {
                e.printStackTrace();
                System.err.println("Error loading model: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error executing EOL script: " + e.getMessage());
        }
    }
    
    /*    private static List<RecommendedItem> rerankRecommendations(List<RecommendedItem> recommendations, String favoriteTOI, Map<Long, Map<String, Object>> itemData) {
            return recommendations.stream()
                .filter(rec -> {
                    Map<String, Object> itemDetails = itemData.get(rec.getItemID());
                    if (itemDetails != null) {
                        Object toiObject = itemDetails.get("category");
                        return toiObject != null && favoriteTOI.equalsIgnoreCase(toiObject.toString().trim());
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }


    */
    private static Map<Long, List<Object[]>> extractRatingsData(List<?> ratingsList) {
        Map<Long, List<Object[]>> ratingsData = new HashMap<>();
        for (Object obj : ratingsList) {
            if (obj instanceof Map) {
                Map<?, ?> rating = (Map<?, ?>) obj;
                try {
                    Long userId = Long.parseLong(rating.get("userId").toString());
                    Long itemId = Long.parseLong(rating.get("itemId").toString());
                    float value = Float.parseFloat(rating.get("rating").toString()); // Ensure key matches your data ("rating")

                    ratingsData.computeIfAbsent(userId, k -> new ArrayList<>()).add(new Object[]{itemId, value});
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing rating data: " + e.getMessage());
                }
            }
        }
        return ratingsData;
    }


    private static FastByIDMap<PreferenceArray> processData(Map<Long, List<Object[]>> ratingsData) {
        FastByIDMap<PreferenceArray> userData = new FastByIDMap<>();

        ratingsData.forEach((userId, preferences) -> {
            PreferenceArray prefsArray = new GenericUserPreferenceArray(preferences.size());
            for (int i = 0; i < preferences.size(); i++) {
                Object[] prefDetails = preferences.get(i);
                long itemId = (long) prefDetails[0];  // POI ID
                float rating = (float) prefDetails[1];  // Rating value
                prefsArray.setUserID(i, userId);
                prefsArray.setItemID(i, itemId);
                prefsArray.setValue(i, rating);
            }
            userData.put(userId, prefsArray);
        });

        return userData;
    }

    private static EmfModel createEmfModel(String name, String modelUri, String metamodelUri) throws EolModelLoadingException {
        EmfModel emfModel = new EmfModel();
        StringProperties properties = new StringProperties();
        properties.put(EmfModel.PROPERTY_NAME, name);
        properties.put(EmfModel.PROPERTY_METAMODEL_URI, metamodelUri);
        properties.put(EmfModel.PROPERTY_MODEL_URI, modelUri);
        properties.put(EmfModel.PROPERTY_READONLOAD, "true");
        properties.put(EmfModel.PROPERTY_STOREONDISPOSAL, "false");
        properties.put(EmfModel.PROPERTY_EXPAND, "true");

        emfModel.load(properties, (IRelativePathResolver) null);
        return emfModel;
    }
    
    public static EmfModel loadEmfModel(String name, String modelPath, String metamodelUri, boolean readOnLoad, boolean storeOnDisposal) throws Exception {
        EmfModel model = new EmfModel();
        model.setName(name);
        model.setMetamodelUri(metamodelUri);
        model.setModelFile(modelPath);
        model.setReadOnLoad(readOnLoad);
        model.setStoredOnDisposal(storeOnDisposal);
        model.setExpand(true);
        model.load();
        return model;
    }
    



}