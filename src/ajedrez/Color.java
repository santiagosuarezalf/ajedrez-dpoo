
package ajedrez;

/**
 *
 * @author juanj
 */
public enum Color{
  BLANCO,NEGRO;

  public Color opuesto(){
    return this == BLANCO ? NEGRO : BLANCO;
  }
}

