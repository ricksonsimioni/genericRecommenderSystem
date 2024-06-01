package genericRecommenderSystem.genericRecommenderSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;

public class Main {

    public static void main(String[] args) {
        try {
            ResourceSet resourceSetTRS = new ResourceSetImpl();
            resourceSetTRS.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            Resource ecoreResourceTRS = resourceSetTRS.getResource(URI.createFileURI("src/main/Models/recommendersystemGeneric.ecore"), true);

            ResourceSet resourceSetDomain = new ResourceSetImpl();
            resourceSetDomain.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            Resource ecoreResourceDomain = resourceSetDomain.getResource(URI.createFileURI("src/main/Models/domain.ecore"), true);

            EPackage ePackageRS = (EPackage) ecoreResourceTRS.getContents().get(0);
            EPackage ePackageDomain = (EPackage) ecoreResourceDomain.getContents().get(0);

            EPackage.Registry.INSTANCE.put(ePackageRS.getNsURI(), ePackageRS);
            EPackage.Registry.INSTANCE.put(ePackageDomain.getNsURI(), ePackageDomain);


            IEolModule module = new EolModule();
            module.parse(new File("src/main/Models/EOL_scripts/dataExtraction.eol"));
            if (module.getParseProblems().size() > 0) {
                System.err.println("Parse problems occurred: " + module.getParseProblems());
            }


            try {
                List<IModel> models = new ArrayList<>();
                
                models.add(loadEmfModel("recommendersystemModel", new File("src/main/Models/recommendersystemGeneric.model").getAbsolutePath(), "http://org.rs", true, false));
                models.add(loadEmfModel("domain", new File("src/main/Models/domain.model").getAbsolutePath(), "http://org.rs.domain", true, false));
                for (IModel model : models) {
                    module.getContext().getModelRepository().addModel(model);
                }

                Object result = module.execute();

                for (IModel model : models) {
                    model.dispose();
                }

                EEnum toiEnum = (EEnum) ePackageDomain.getEClassifier("Category");
                List<String> toiValues = new ArrayList<>();

                if (toiEnum != null && toiEnum.getELiterals() != null) {
                    for (EEnumLiteral literal : toiEnum.getELiterals()) {
                        toiValues.add(literal.getName());
                    }
                }

                if (result instanceof Map) {
                    Map<?, ?> resultMap = (Map<?, ?>) result;

                    Object ratingsObj = resultMap.get("ratingsData");
                    FastByIDMap<PreferenceArray> mahoutData = null;
                    if (ratingsObj instanceof List) {
                        mahoutData = processData(extractRatingsData((List<?>) ratingsObj));
                    }
                    Map<Integer, Map<String, Object>> itemData = extractItemData(resultMap);

                    if (mahoutData != null) {
					    DataModel model = new GenericDataModel(mahoutData);
					    UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
					    UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
					    GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
					    try (Scanner scanner = new Scanner(System.in)) {
					        System.out.print("Please enter the user ID for which you want recommendations: ");
					        long userId = scanner.nextLong();
					
					        System.out.println("What's your favorite topic of interest?" + toiValues);
					        String favoriteTOI = scanner.next();
					
					        List<RecommendedItem> recommendations = recommender.recommend(userId, 10);
					        List<RecommendedItem> rerankedRecommendations = rerankRecommendations(recommendations, favoriteTOI, itemData);
			
					        System.out.println("Recommendations for user ID: " + userId);
					        for (RecommendedItem recommendation : rerankedRecommendations) {
					        	int itemId = (int) recommendation.getItemID();
					            String poiName = (itemData.containsKey(itemId)) && itemData.get(itemId).containsKey("itemName") 
					                             ? itemData.get(itemId).get("itemName").toString() 
					                             : "Unknown POI";
					            System.out.println("Recommended POI: " + poiName + " - Score: " + recommendation.getValue());
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

    private static List<RecommendedItem> rerankRecommendations(List<RecommendedItem> recommendations, String favoriteTOI, Map<Integer, Map<String, Object>> itemData) {
        return recommendations.stream()
            .filter(rec -> {
                Map<String, Object> itemDetails = itemData.get((int) rec.getItemID()); // Cast item ID to int
                if (itemDetails != null) {
                    Object toiObject = itemDetails.get("category");
                    return toiObject != null && favoriteTOI.equalsIgnoreCase(toiObject.toString().trim());
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    
    public static Map<Integer, Map<String, Object>> extractItemData(Map<?, ?> resultMap) {
        Map<Integer, Map<String, Object>> itemData = new HashMap<>();
        
        if (resultMap instanceof Map) {
            Object itemDataObj = resultMap.get("itemData");
            if (itemDataObj instanceof Map) {
                Map<?, ?> rawItemData = (Map<?, ?>) itemDataObj;
                if (!rawItemData.isEmpty()) {
                    rawItemData.forEach((key, value) -> {
                        if (value instanceof Map) {
                            Map<?, ?> details = (Map<?, ?>) value;
                            if (details.containsKey("itemId")) {
                                Object itemIdObj = details.get("itemId");
                                if (itemIdObj instanceof Integer) {
                                    Integer itemId = (Integer) itemIdObj;
                                    Map<String, Object> itemDetails = new HashMap<>();
                                    details.forEach((detailKey, detailValue) -> {
                                        if (detailKey instanceof String) {
                                            itemDetails.put((String) detailKey, detailValue);
                                        }
                                    });
                                    itemData.put(itemId, itemDetails);
                                } else {
                                    System.out.println("itemId is not an integer.");
                                }
                            } else {
                                System.out.println("itemId is missing.");
                            }
                        }
                    });
                } else {
                    System.out.println("Item Data is empty.");
                }
            } else {
                System.out.println("Item Data object is not a map.");
            }
        } else {
            System.out.println("Result object is not a map.");
        }
        
        return itemData;
    }


    private static Map<Long, List<Object[]>> extractRatingsData(List<?> ratingsList) {
        Map<Long, List<Object[]>> ratingsData = new HashMap<>();
        for (Object obj : ratingsList) {
            if (obj instanceof Map) {
                Map<?, ?> rating = (Map<?, ?>) obj;
                try {
                    Long userId = Long.parseLong(rating.get("userId").toString());
                    Long itemId = Long.parseLong(rating.get("itemId").toString());
                    float value = Float.parseFloat(rating.get("rating").toString());

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
                long itemId = (long) prefDetails[0];
                float rating = (float) prefDetails[1];
                prefsArray.setUserID(i, userId);
                prefsArray.setItemID(i, itemId);
                prefsArray.setValue(i, rating);
            }
            userData.put(userId, prefsArray);
        });

        return userData;
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
