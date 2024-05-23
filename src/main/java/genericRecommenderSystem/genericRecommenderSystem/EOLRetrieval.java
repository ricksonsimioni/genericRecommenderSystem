package genericRecommenderSystem.genericRecommenderSystem;

import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EOLRetrieval {

    static {
        try {
            // Register the Ecore Resource Factory
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

            // Correctly load the Ecore file
            ResourceSet resourceSet = new ResourceSetImpl();
            Resource ecoreResource = resourceSet.getResource(URI.createFileURI("src/main/Models/recommendersystemGeneric.ecore"), true);
            
            // Assuming you want to work with the root EPackage
            EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);
            EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);

            System.out.println("EPackage registered: " + EPackage.Registry.INSTANCE.get(ePackage.getNsURI()));

            // Register the second Ecore file (domain.ecore)
            ResourceSet resourceSetDomain = new ResourceSetImpl();
            Resource ecoreResourceDomain = resourceSetDomain.getResource(URI.createFileURI("src/main/Models/domain.ecore"), true);
            EPackage ePackageDomain = (EPackage) ecoreResourceDomain.getContents().get(0);
            EPackage.Registry.INSTANCE.put(ePackageDomain.getNsURI(), ePackageDomain);

            System.out.println("EPackage Domain registered: " + EPackage.Registry.INSTANCE.get(ePackageDomain.getNsURI()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // Initialize the EOL module
        IEolModule module = new EolModule();

        // Load your EOL script
        File scriptFile = new File("src/main/Models/EOL_scripts/dataExtraction.eol");
        module.parse(scriptFile);
        
        

        // Ensure the script is parsed correctly
        if (module.getParseProblems().size() > 0) {
            System.err.println("Parse errors occurred...");
            for (ParseProblem error : module.getParseProblems()) {
                System.err.println(error);
            }
            return;
        }

        // Load the EMF models
        List<IModel> models = new ArrayList<>();
        models.add(loadEmfModel("recommendersystemModel", "src/main/Models/recommendersystemGeneric.model", true, false));

        // Add models to the EOL module
        for (IModel model : models) {
            module.getContext().getModelRepository().addModel(model);
        }
        
        System.out.println("Models in repository:");
        for (Object obj : module.getContext().getModelRepository().getModels()) {
            System.out.println(obj);
        }


        // Execute the script
        Object result = module.execute();

        // Handle the result if needed
        System.out.println("Result: " + result);

        // Dispose the models
        for (IModel model : models) {
            model.dispose();
        }
    }

    public static EmfModel loadEmfModel(String name, String modelPath, boolean readOnLoad, boolean storeOnDisposal) throws Exception {
        EmfModel model = new EmfModel();
        model.setName(name);
        model.setModelFile(modelPath);
        model.setReadOnLoad(readOnLoad);
        model.setStoredOnDisposal(storeOnDisposal);
        model.load();
        return model;
    }
}
