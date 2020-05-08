package render.mesh;

import org.joml.Vector3f;

public class Material {
	public final String name;
	public Vector3f ambientColor = new Vector3f(1, 1, 1);
	public Vector3f diffuseColor = new Vector3f(1, 1, 1);
	public Vector3f specularColor = new Vector3f(1, 1, 1);
	public float specularExponent;
	
	public float refraction;
	public float opacity;
	
	public int illum;
	
	public Texture texture;
	
	/*
	public Texture abientTextureMap;
	public Texture diffuseTextureMap;
	public Texture specularColorMap;
	public Texture specularComponentMap;
	public Texture alphaTextureMap;
	public Texture bumpMap;
	public Texture dispMap;
	public Texture */
	
	public Material(String name) {
		this.name = name;
	}

	public void cleanup() {
		if(texture != null) {
			texture.cleanUp();
		}
	}
}
