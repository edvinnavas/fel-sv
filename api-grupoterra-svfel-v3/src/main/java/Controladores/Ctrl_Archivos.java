package Controladores;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ctrl_Archivos implements Serializable {

    private static final long serialVersionUID = 1L;

    public Ctrl_Archivos() {

    }

    public List<String> lineas_archivo(String nombre_archivo) {
        List<String> resultado = new ArrayList<>();

        try {
            FileReader fr = new FileReader(nombre_archivo);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            while((linea = br.readLine()) != null) {
                resultado.add(linea);
            }
            br.close();
            fr.close();
        } catch (Exception ex) {
            resultado = null;
            System.out.println("PROYECTO: API-GRUPOTERRA-SVFEL-V3 ==> CLASE: " + this.getClass().getName()
                    + " ==> METODO: lineas_archivo()" + " ERROR: " + ex.toString());
        }

        return resultado;
    }

}