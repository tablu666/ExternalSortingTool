// WARNING: This program uses the Assertion class. When it is run,
// assertions must be turned on. For example, under Linux, use:
// java -ea Genfile

/**
 * Generate a data file. The size is a multiple of 8192 bytes.
 * Each record is one long and one double.
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Generates binary files
 * 
 * @author CS Staff
 * @version 2020-10-15
 */
public class GenFile {

    /**
     * Max num of records
     */
    static final int NUMRECS = 512; // Because they are short ints

    /** Initialize the random variable */
    static private Random value = new Random(); // Hold the Random class object

    /**
     * Constructor
     */
    public GenFile() {
        // Here for completeness.
    }


    /**
     * Gets a random long
     * 
     * @return a random long
     */
    static long randLong() {
        return value.nextLong();
    }


    /**
     * Gets a random double
     * 
     * @return a random double
     */
    static double randDouble() {
        return value.nextDouble();
    }


    /**
     * Creates a bin file with random values
     * 
     * @param args
     *            string array of arguments
     * @throws IOException
     */
    public static void random(String[] args) throws IOException {
        long val;
        double val2;
        if (args.length != 2) {
            System.out.println("\nUsage: Genfile <filename> <size>"
                + "\nOptions \nSize is measured in blocks of 8192 bytes");

        }
        else {
            int filesize = Integer.parseInt(args[1]); // Size of file in blocks
            DataOutputStream file = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(args[0])));
            for (int i = 0; i < filesize; i++) {
                for (int j = 0; j < NUMRECS; j++) {
                    val = (long)(randLong());
                    file.writeLong(val);
                    val2 = (double)(randDouble());
                    file.writeDouble(val2);
                }
            }
            file.flush();
            file.close();
        }
    }


    /**
     * Creates a bin file with sorted values
     * 
     * @param args
     *            string array of arguments
     * @throws IOException
     */
    public static void sorted(String[] args) throws IOException {
        long val;
        double val2;

        int filesize = Integer.parseInt(args[1]); // Size of file in blocks
        DataOutputStream file = new DataOutputStream(new BufferedOutputStream(
            new FileOutputStream(args[0])));
        // For sorted.

        for (int i = 0; i < filesize; i++) {
            for (int j = 0; j < NUMRECS; j++) {
                val = (long)(i * 512 + j);
                file.writeLong(val);
                val2 = (double)(i * 512 + j);
                file.writeDouble(val2);
            }
        }
        file.flush();
        file.close();
    }


    /**
     * Creates a bin file with reverse sorted values
     * 
     * @param args
     *            string array of arguments
     * @throws IOException
     */
    public static void reversed(String[] args) throws IOException {
        long val;
        double val2;
        int filesize = Integer.parseInt(args[1]); // Size of file in blocks
        DataOutputStream file = new DataOutputStream(new BufferedOutputStream(
            new FileOutputStream(args[0])));
        // For sorted.

        for (int i = filesize - 1; i >= 0; i--) {
            for (int j = NUMRECS - 1; j >= 0; j--) {
                val = (long)(i * 512 + j);
                file.writeLong(val);
                val2 = (double)(i * 512 + j);
                file.writeDouble(val2);
            }
        }
        file.flush();
        file.close();
    }

}
