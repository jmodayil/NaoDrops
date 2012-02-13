package wekaTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;

import org.apache.commons.lang3.ArrayUtils;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import AudioPrediction.MFCCProvider;


public class createMFCC_ARFFfromRaw {

  /**
   * @param args
   * @throws ParseException
   * @throws IOException
   */
  public static void main(String[] args) throws ParseException, IOException {

    double[][][] featureData;
    FastVector names = new FastVector();
    for (int n = 0; n < args.length; n++) {
      names.addElement(new String("Speaker" + n));
    }


    featureData = readInput(args);

    FastVector atts;
    Instances data;
    double[] vals;

    for (int n = 0; n < featureData.length; n++) {
      System.out.println((String) names.elementAt(n));
    }

    // 1. set up attributes
    atts = new FastVector();
    // - numeric
    for (int n = 0; n < featureData[0][0].length; n++) {
      atts.addElement(new Attribute("feature" + n));
    }
    atts.addElement(new Attribute("class", names));


    // 2. create Instances object
    data = new Instances("MyRelation", atts, 0);

    // 3. fill with data
    for (int person = 0; person < featureData.length; person++) {
      for (int sample = 0; sample < featureData[0].length; sample++) {
        vals = new double[data.numAttributes()];
        for (int feature = 0; feature < featureData[0][0].length; feature++) {
          vals[feature] = featureData[person][sample][feature];
        }
        vals[data.numAttributes() - 1] = data.attribute(data.numAttributes() - 1).indexOfValue((String) names
                                                                                                   .elementAt(person));
        System.out.println("VALS [  ]: " + vals[data.numAttributes() - 1]);
        data.add(new Instance(1.0, vals));
      }
    }

    // Save the data to a file

    ArffSaver saver = new ArffSaver();
    File outputFile = new File(args[args.length - 1] + ".arff");
    if (outputFile.exists()) {
      System.out.println("Output File exists already! ABORTING!");
      return;
    }
    saver.setInstances(data);
    saver.setFile(outputFile);
    saver.writeBatch();
  }

  private static double[][][] readInput(String[] args) throws IllegalArgumentException, IOException {
    int length = args.length;
    Double[][][] data = new Double[length][][];

    double[][][] realData = new double[length][][];

    double[][][] realMFCCdata;
    MFCCProvider toMFCC = new MFCCProvider();


    FileInputStream fis = null;
    ObjectInputStream in = null;

    // Read the Files:
    for (int n = 0; n < length; n++) {
      try {
        System.out.println(args[n]);
        fis = new FileInputStream(args[n]);
        in = new ObjectInputStream(fis);
        data[n] = ((Double[][]) in.readObject());
        in.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
    }

    for (int m = 0; m < data.length; m++) {
      realData[m] = new double[data[0].length][];
      for (int n = 0; n < data[0].length; n++) {
        realData[m][n] = ArrayUtils.toPrimitive(data[m][n]);
      }
    }

    realMFCCdata = toMFCC.processTestData(realData);

    System.out.println("Dimensions of feature Data: " + realMFCCdata.length + " "
        + realMFCCdata[realMFCCdata.length - 1].length + " "
        + realMFCCdata[realMFCCdata.length - 1][realMFCCdata[realMFCCdata.length - 1].length - 1].length);

    return realMFCCdata;
  }
}