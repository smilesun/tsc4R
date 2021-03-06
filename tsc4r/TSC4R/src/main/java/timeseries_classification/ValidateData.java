package timeseries_classification;
import com.sun.xml.internal.fastinfoset.util.StringArray;
import utilities.ClassifierTools;
import weka.classifiers.Classifier;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ValidateData {
    public boolean validationForTrain(Instances data) throws IOException {
        // check whether data has class column
        String name =data.attribute(data.classIndex()).name();
        boolean errFlag = true;
        if (!name.equals("class")&&!name.equals("target")){
            System.err.println("Error! the data don't have class column");
            errFlag = false;

        }
        for (int i = 0; i < data.numAttributes();i++){
            AttributeStats as = data.attributeStats(i);
            int percent = (int) Math.round(100.0 * as.missingCount / as.totalCount);
            if(percent>0){
                String n = data.attribute(i).name();
                System.err.println(String.format("Error! The data missing percentage of column %s is %d percent", n,percent));
                errFlag = false;
            }
        }

        if (errFlag == false){
            throw new IOException("Error in dataset");
        }
        return errFlag;
    }
    public boolean validationForPrediction(Instances data) throws IOException {
        boolean errFlag = true;
        for (int i = 0; i < data.numAttributes()-1;i++){
            AttributeStats as = data.attributeStats(i);
            int percent = (int) Math.round(100.0 * as.missingCount / as.totalCount);
            if(percent>0){
                String n = data.attribute(i).name();
                System.err.println(String.format("Error! The data missing percentage of column %s is %d percent", n,percent));
                errFlag = false;
            }
        }

        if (errFlag == false){
            throw new IOException("Error in dataset");
        }
        return errFlag;
    }

    public boolean unitTest(String[] params) throws Exception {
        //test whether the prediction performs the same for the data with/without target column
        String moduleAddress=params[1];
        String testData = params[4];

        // training classifier
        String[] paramsForTrain=Arrays.copyOfRange(params,0,4);
        System.out.println();
        HandleChain train = new Train();
        train.process(paramsForTrain);

        // test classifier
        Instances test = ClassifierTools.loadData(testData);
        ValidateData v = new ValidateData();
        boolean f = v.validationForTrain(test);
        Classifier c=null;

        //read obj from file
        FileInputStream fi = new FileInputStream(new File(moduleAddress));
        ObjectInputStream oi = new ObjectInputStream(fi);
        try {
            c=(Classifier) oi.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("there is no classifiers on this path");
        }
        oi.close();
        fi.close();

        //test with target
        System.out.println("number of attributes"+test.numAttributes());
        double[] resultsWithTarget=new double[test.numInstances()];
        for(int i=0;i<test.numInstances();i++){
            resultsWithTarget[i]=c.classifyInstance(test.instance(i));
        }

        System.out.println("prediction result with target");
        for (double r :
                resultsWithTarget) {
            System.out.print(r+" ");
        }
        System.out.println();
        double acc = ClassifierTools.accuracy(test,c);
        System.out.println(acc);

        // test without target

        test.setClassIndex(test.numAttributes()-2);
        test.deleteAttributeAt(test.numAttributes()-1);
        System.out.println("number of attributes"+test.numAttributes());
        double[] resultsWithoutTarget=new double[test.numInstances()];
        for(int i=0;i<test.numInstances();i++){
            resultsWithoutTarget[i]=c.classifyInstance(test.instance(i));
        }

        System.out.println("prediction result without target");
        for (double r :
                resultsWithoutTarget) {
            System.out.print(r+" ");
        }
        System.out.println();

        for (int i = 0; i <resultsWithTarget.length; i ++){
            if (resultsWithTarget[i] != resultsWithoutTarget[i]){
                return false;
            }
        }
        return true;

    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader("/Users/wangyu/Documents/LMU/tsc4R/TimeSeriesClassification/src/timeseries_classification/unitTestArgs"));
        String line;
        ArrayList<String> params = new ArrayList<>();
        while ((line = in.readLine())!=null){

            if (!line.equals( "unitTest")){
                params.add(line);
            }
            else {
                String data = params.get(0);
                String[] d = data.split("\\/");
                System.out.println();
                System.out.println(d[d.length-1]);
                ValidateData v = new ValidateData();
                String[] pa = new String[params.size()];
                for (int i = 0; i < params.size(); i ++){
                    pa[i] = params.get(i);
                }
                params.clear();

                boolean testResult = v.unitTest(pa);
                System.out.println(testResult);
            }
        }
        in.close();
    }
}
