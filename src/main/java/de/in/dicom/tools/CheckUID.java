package de.in.dicom.tools;

import org.dcm4che3.data.UID;

public class CheckUID {
    public static void main(String[] args) {
        String input = args.length > 0 ? args[0] : "1.2.3.4";
        String name = UID.nameOf(input);
        System.out.println("Input: " + input);
        System.out.println("Name: " + name);
        System.out.println("Equals: " + input.equals(name));
    }
}
