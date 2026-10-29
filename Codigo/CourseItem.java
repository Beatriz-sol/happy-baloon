package br.mackenzie.world;

/**
 * Moeda ou pássaro posicionado ao longo do percurso.
 * A posição x é medida em distância percorrida, não em coordenada de tela.
 */
public class CourseItem {
    public enum Type { COIN, BIRD }

    public final Type type;
    public final float distance;
    public final float y;
    public final float radius;

    // Moeda coletada ou pássaro já atingido: não conta de novo
    public boolean used;

    public CourseItem(Type type, float distance, float y, float radius) {
        this.type = type;
        this.distance = distance;
        this.y = y;
        this.radius = radius;
    }
}
