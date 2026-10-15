/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ajedrez;

/**
 *
 * @author juanj
 */

import java.util.Scanner;

public class Ajedrez {

    public static void main(String[] args) {
        Juego juego = new Juego();
        Scanner sc = new Scanner(System.in);

        while (!juego.isTerminado()) {
            juego.getTablero().imprimir();
            System.out.println("Turno: " + juego.getTurno());
            if (juego.estaEnJaque(juego.getTurno())) System.out.println("¡JAQUE!");
            System.out.print("Movimiento (ej. e2 e4) o 'salir': ");
            String linea = sc.nextLine().trim().toLowerCase();
            if (linea.equals("salir")) break;

            String[] partes = linea.split("\\s+");
            if (partes.length != 2) { System.out.println("Formato: origen destino"); continue; }

            Casilla o = parsearCasilla(juego.getTablero(), partes[0]);
            Casilla d = parsearCasilla(juego.getTablero(), partes[1]);
            if (o == null || d == null) { System.out.println("Casilla inválida."); continue; }

            if (!juego.mover(o, d)) System.out.println("Movimiento ilegal.");
        }

        juego.getTablero().imprimir();
        if (juego.getResultado() != null) System.out.println(juego.getResultado());
    }

    private static Casilla parsearCasilla(Tablero t, String s) {
        if (s.length() != 2) {
          return null;
        }
        int c = s.charAt(0) - 'a';
        int f = 8 - (s.charAt(1) - '0');
        return t.getCasilla(f, c);
    }
}