/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package evosimbad.core;

/**
 *
 * @author Jorge
 */
public class Test {

    public static void main(String[] args) {
        String line = "56,\"f1(2,3)\",,\"f2(4,5)\", ";
        String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String t : tokens) {
            System.out.println("> " + t);
        }
    }
}
