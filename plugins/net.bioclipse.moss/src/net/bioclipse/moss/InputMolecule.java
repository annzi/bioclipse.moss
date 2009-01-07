/*******************************************************************************
 *Copyright (c) 2008 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package net.bioclipse.moss;

/**
 * Base class describing a molecule/entry in MOSS
 * 
 * @author Annsofie Andersson
 * 
 */
public class InputMolecule {

    private String id;
    private float value;
    private String description;
    private boolean checked;

    /** Constructor, defines the input molecule by identity (a,b,c..etc) a associated value
     and a molecule written in SMILES*/
    public InputMolecule(String id, float value, String description) {
        super();
        this.id = id;
        this.value = value;
        this.description = description;
        checked = true;
    }

    /* Getters and setters for the attributes of Input molecule*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
