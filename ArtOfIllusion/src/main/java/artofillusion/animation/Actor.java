/* Copyright (C) 2003 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.*;
import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * An Actor is an object with a set of predefined gestures. Gestures can be blended in arbitrary
 * combinations to form poses.
 */
@Slf4j
public class Actor extends ObjectWrapper {

    Gesture[] gesture;
    String[] gestureName;
    int[] gestureID;
    int nextPoseID;
    private ActorKeyframe currentPose;

    public Actor(Object3D obj) {
        theObject = obj;
        gesture = new Gesture[]{(Gesture) obj.getPoseKeyframe()};
        gestureName = new String[]{"Default Pose"};
        gestureID = new int[]{0};
        nextPoseID = 1;
        currentPose = new ActorKeyframe();
    }

    /**
     * Add a new gesture to this actor.
     */
    public void addGesture(Gesture p, String name) {
        int num = gesture.length;
        var newPose = new Gesture[num + 1];
        var newName = new String[num + 1];
        var newID = new int[num + 1];
        System.arraycopy(gesture, 0, newPose, 0, num);
        System.arraycopy(gestureName, 0, newName, 0, num);
        System.arraycopy(gestureID, 0, newID, 0, num);
        newPose[num] = p;
        newName[num] = name;
        newID[num] = nextPoseID++;
        gesture = newPose;
        gestureName = newName;
        gestureID = newID;
    }

    /**
     * Delete a gesture from this actor.
     */
    public void deleteGestureWithID(int id) {
        int which = getGestureIndex(id);
        if (which == -1) {
            return;
        }
        int num = gesture.length;
        var newPose = new Gesture[num - 1];
        var newName = new String[num - 1];
        int[] newID = new int[num - 1];
        int j = 0;
        for (int i = 0; i < num; i++) {
            if (i == which) {
                continue;
            }
            newPose[j] = gesture[i];
            newName[j] = gestureName[i];
            newID[j] = gestureID[i];
            j++;
        }
        gesture = newPose;
        gestureName = newName;
        gestureID = newID;
    }

    /**
     * Get the number of gestures defined for this actor.
     */
    public int getNumGestures() {
        return gesture.length;
    }

    /**
     * Get the i'th gesture defined for this actor.
     */
    public Gesture getGesture(int i) {
        return gesture[i];
    }

    /**
     * Get the gesture with a particular ID, or null if there is no gesture with that ID.
     */
    public Gesture getGestureWithID(int id) {
        int index = getGestureIndex(id);
        return (index == -1 ? null : gesture[index]);
    }

    /**
     * Get the name of the i'th gesture defined for this actor.
     */
    public String getGestureName(int i) {
        return gestureName[i];
    }

    /**
     * Set the name of the i'th gesture defined for this actor.
     */
    public void setGestureName(int i, String name) {
        gestureName[i] = name;
    }

    /**
     * Get the ID of the i'th gesture defined for this actor.
     */
    public int getGestureID(int i) {
        return gestureID[i];
    }

    /**
     * Return the index of the gesture with a particular ID, or -1 if there is no gesture with
     * that ID.
     */
    public int getGestureIndex(int id) {
        int which = 0;
        for (int cg : gestureID) {
            if (cg == id) {
                break;
            }
            which++;
        }
        return which == gestureID.length ? -1 : which;
    }

    /**
     * Create a new object which is an exact duplicate of this one.
     */
    @Override
    public Actor duplicate() {
        Actor a = new Actor(theObject.duplicate());
        a.gesture = new Gesture[gesture.length];
        a.gestureName = new String[gesture.length];
        a.gestureID = new int[gesture.length];
        for (int i = 0; i < gesture.length; i++) {
            a.gesture[i] = (Gesture) gesture[i].duplicate(a.theObject);
            a.gestureName[i] = gestureName[i];
            a.gestureID[i] = gestureID[i];
        }
        a.nextPoseID = nextPoseID;
        a.currentPose = (ActorKeyframe) currentPose.duplicate(a);
        return a;
    }

    /**
     * Copy all the properties of another object, to make this one identical to it. If the
     * two objects are of different classes, this will throw a ClassCastException.
     */
    @Override
    public void copyObject(Object3D obj) {
        Actor a = (Actor) obj;
        theObject = a.theObject.duplicate();
        gesture = new Gesture[a.gesture.length];
        gestureName = new String[a.gesture.length];
        gestureID = new int[a.gesture.length];
        for (int i = 0; i < gesture.length; i++) {
            gesture[i] = (Gesture) a.gesture[i].duplicate(theObject);
            gestureName[i] = a.gestureName[i];
            gestureID[i] = a.gestureID[i];
        }
        nextPoseID = a.nextPoseID;
        currentPose = (ActorKeyframe) a.currentPose.duplicate(this);
    }

    /**
     * The size of an Actor cannot be set directly, since that is determined by its Poses.
     */
    @Override
    public void setSize(double xsize, double ysize, double zsize) {
    }

    /**
     * If the object can be edited by the user, isEditable() should be overridden to return true.
     * edit() should then create a window and allow the user to edit the object.
     */
    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void edit(EditingWindow parent, ObjectInfo info, Runnable cb) {
        new ActorEditorWindow(parent, info, this, null, cb);
    }

    /**
     * All of the following methods call through to the corresponding methods on the object.
     */
    @Override
    public boolean canSetTexture() {
        return theObject.canSetTexture();
    }

    @Override
    public boolean canSetMaterial() {
        return theObject.canSetMaterial();
    }

    @Override
    public void setTexture(Texture tex, TextureMapping map) {
        TextureParameter[] oldParam = getParameters();
        theObject.setTexture(tex, map);
        TextureParameter[] newParam = map.getParameters();
        for (Gesture value : gesture) {
            value.textureChanged(oldParam, newParam);
        }
    }

    @Override
    public void setMaterial(Material mat, MaterialMapping map) {
        theObject.setMaterial(mat, map);
    }

    /**
     * Set the list of objects defining the values of texture parameters.
     */
    @Override
    public void setParameterValues(ParameterValue[] val) {
        // Set them on the current pose.

        theObject.setParameterValues(val);

        // Set them on every gesture.
        TextureParameter[] params = getParameters();
        for (int i = 0; i < gesture.length; i++) {
            for (int j = 0; j < params.length; j++) {
                gesture[i].setTextureParameter(params[j], val[j].duplicate());
            }
        }
    }

    /**
     * Set the values of a texture parameter in every gesture.
     */
    @Override
    public void setParameterValue(TextureParameter param, ParameterValue value) {
        // Set it on the current pose.

        theObject.setParameterValue(param, value);

        // Set it on every gesture.
        for (int i = 0; i < gesture.length; i++) {
            gesture[i].setTextureParameter(param, value.duplicate());
        }
    }

    /**
     * Given an object (either this Actor's object or a duplicate of it), reshape the object based
     * on this Actor's gestures. This function examines the object's skeleton, finds the combination
     * of gestures that most nearly reproduce that skeleton shape, and then adjusts all of the vertex
     * positions based on the gestures.
     */
    public void shapeMeshFromGestures(Object3D obj) {
        Skeleton skeleton = obj.getSkeleton();
        Skeleton defaultSkeleton = gesture[0].getSkeleton();
        Joint[] currentJoint = skeleton.getJoints();
        Joint[] defaultJoint = defaultSkeleton.getJoints();
        int numDof = currentJoint.length * 4;
        double[][] coeff = new double[numDof][gesture.length - 1];
        double[] goal = new double[numDof];

        // Find the right hand side vector.
        double d = Math.PI / 180.0;
        for (int i = 0; i < defaultJoint.length; i++) {
            goal[i * 4] = d * (currentJoint[i].angle1.pos - defaultJoint[i].angle1.pos);
            goal[i * 4 + 1] = d * (currentJoint[i].angle2.pos - defaultJoint[i].angle2.pos);
            goal[i * 4 + 2] = d * (currentJoint[i].twist.pos - defaultJoint[i].twist.pos);
            goal[i * 4 + 3] = currentJoint[i].length.pos - defaultJoint[i].length.pos;
        }

        // Find the matrix of coefficients.
        for (int j = 1; j < gesture.length; j++) {
            Joint[] gestureJoint = gesture[j].getSkeleton().getJoints();
            for (int i = 0; i < defaultJoint.length; i++) {
                coeff[i * 4][j - 1] = d * (gestureJoint[i].angle1.pos - defaultJoint[i].angle1.pos);
                coeff[i * 4 + 1][j - 1] = d * (gestureJoint[i].angle2.pos - defaultJoint[i].angle2.pos);
                coeff[i * 4 + 2][j - 1] = d * (gestureJoint[i].twist.pos - defaultJoint[i].twist.pos);
                coeff[i * 4 + 3][j - 1] = gestureJoint[i].length.pos - defaultJoint[i].length.pos;
            }
        }

        // Solve the equations.  We only want to use gestures with positive weights, since many
        // gestures are not designed to look right when applied with negative weights.  Eventually,
        // I should rewrite this to use a proper non-negative least squares algorithm.  For the
        // moment, it uses a simpler approximate method: solve the equations using SVD, then check
        // the weights.  If any are negative, remove those gestures and repeat until all weights
        // are non-negative.
        boolean converged = false;
        boolean[] negative = new boolean[gesture.length - 1];
        double[] weight = new double[gesture.length - 1];
        while (!converged) {
            // Build the matrix to solve.

            int numGesture = 0;
            for (int i = 0; i < negative.length; i++) {
                if (!negative[i]) {
                    numGesture++;
                }
            }
            if (numGesture == 0) {
                MeshGesture average = (MeshGesture) obj.getPoseKeyframe();
                ((MeshGesture) gesture[0]).blendSurface(average, new MeshGesture[0], new double[0]);
                obj.applyPoseKeyframe(average);
                return;
            }
            double[] rhs = new double[Math.max(numDof, numGesture)];
            for (int i = 0; i < numDof; i++) {
                rhs[i] = goal[i];
            }
            double[][] matrix = new double[numDof][numGesture];
            for (int i = 0, j = 0; i < negative.length; i++) {
                if (!negative[i]) {
                    for (int k = 0; k < numDof; k++) {
                        matrix[k][j] = coeff[k][i];
                    }
                    j++;
                }
            }
            SVD.solve(matrix, rhs, 0.1);

            // Check for negative weights.
            converged = true;
            for (int i = 0, j = 0; i < negative.length; i++) {
                weight[i] = 0.0;
                if (!negative[i]) {
                    if (rhs[j] < -0.001) {
                        negative[i] = true;
                        converged = false;
                    } else if (rhs[j] > 0.001) {
                        weight[i] = rhs[j];
                    }
                    j++;
                }
            }
        }

        // Construct the output pose.
        List<Gesture> gestureList = new Vector<>();
        List<Double> weightList = new Vector<>();

        for (int i = 0; i < weight.length; i++) {
            if (weight[i] > 0.0) {
                gestureList.add(gesture[i + 1]);
                weightList.add(weight[i]);
            }
        }
        MeshGesture[] poseGesture = new MeshGesture[gestureList.size()];
        double[] poseWeight = new double[weightList.size()];
        for (int i = 0; i < poseGesture.length; i++) {
            poseGesture[i] = (MeshGesture) gestureList.get(i);
            poseWeight[i] = weightList.get(i);
        }
        MeshGesture average = (MeshGesture) obj.getPoseKeyframe();
        ((MeshGesture) gesture[0]).blendSurface(average, poseGesture, poseWeight);
        obj.applyPoseKeyframe(average);
    }

    /**
     * Write a representation of this object to a file.
     */
    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(0);
        out.writeUTF(theObject.getClass().getName());
        theObject.writeToFile(out, theScene);
        out.writeInt(nextPoseID);
        out.writeUTF(gesture[0].getClass().getName());
        out.writeInt(gesture.length);
        for (int i = 0; i < gesture.length; i++) {
            out.writeInt(gestureID[i]);
            out.writeUTF(gestureName[i]);
            gesture[i].writeToStream(out);
        }
        currentPose.writeToStream(out);
    }

    /**
     * Reconstruct this object from its serialized representation.
     */
    public Actor(DataInputStream in, Scene theScene) throws IOException {
        short version = in.readShort();
        if (version != 0) {
            throw new InvalidObjectException("");
        }
        try {
            Class<?> cls = ArtOfIllusion.getClass(in.readUTF());
            Constructor<?> con = cls.getConstructor(DataInputStream.class, Scene.class);
            theObject = (Object3D) con.newInstance(in, theScene);
            nextPoseID = in.readInt();
            cls = ArtOfIllusion.getClass(in.readUTF());
            con = cls.getConstructor(DataInputStream.class, Object.class);
            int num = in.readInt();
            gesture = new Gesture[num];
            gestureName = new String[num];
            gestureID = new int[num];
            for (int i = 0; i < num; i++) {
                gestureID[i] = in.readInt();
                gestureName[i] = in.readUTF();
                gesture[i] = (Gesture) con.newInstance(in, theObject);
            }
            currentPose = new ActorKeyframe(in, this);
        } catch (InvocationTargetException ex) {
            log.atError().setCause(ex.getCause()).log("Error creating Actor: {}", ex.getCause().getMessage());
            throw new IOException();
        } catch (IOException | ReflectiveOperationException | SecurityException ex) {
            log.atError().setCause(ex).log("Error creating Actor: {}", ex.getMessage());
            throw new IOException();
        }
    }

    @Override
    public Property[] getProperties() {
        Property[] prop = new Property[currentPose.getNumGestures()];
        for (int i = 0; i < prop.length; i++) {
            prop[i] = new Property(getGestureName(getGestureIndex(currentPose.getGestureID(i))),
                    -Double.MAX_VALUE, Double.MAX_VALUE, currentPose.getGestureWeight(i));
        }
        return prop;
    }

    @Override
    public Object getPropertyValue(int index) {
        return currentPose.getGestureWeight(index);
    }

    @Override
    public void setPropertyValue(int index, Object value) {
        currentPose.weight[index] = ((Double) value);
        applyPoseKeyframe(currentPose);
    }

    /**
     * Return a Keyframe which describes the current pose of this object.
     */
    @Override
    public Keyframe getPoseKeyframe() {
        return currentPose.duplicate(this);
    }

    /**
     * Modify this object based on a pose keyframe.
     */
    @Override
    public void applyPoseKeyframe(Keyframe k) {
        currentPose = (ActorKeyframe) k.duplicate(this);
        for (int i = 0; i < currentPose.id.length; i++) {
            if (getGestureIndex(currentPose.id[i]) == -1) {
                currentPose.deleteGesture(i);
                i--;
            }
        }
        theObject.applyPoseKeyframe(currentPose.createObjectKeyframe(this));
    }

    /**
     * This will be called whenever a new pose track is created for this object. It allows
     * the object to configure the track by setting its graphable values, subtracks, etc.
     */
    @Override
    public void configurePoseTrack(PoseTrack track) {
        track.setGraphableValues(new String[0], new double[0], new double[0][2]);
    }

    /**
     * Find the array index for a given pose ID.
     */
    int findPoseIndex(int id) {
        int min = 0;
        int max = gestureID.length - 1;
        int current = (min + max) >> 1;

        while (true) {
            if (gestureID[current] == id) {
                return current;
            }
            if (gestureID[current] > id) {
                max = current - 1;
            } else {
                min = current + 1;
            }
            if (min >= max) {
                if (min < gestureID.length && gestureID[min] == id) {
                    return min;
                }
                return -1;
            }
            current = (min + max) >> 1;
        }
    }

    /**
     * Allow the user to edit a keyframe returned by getPoseKeyframe().
     */
    @Override
    public void editKeyframe(EditingWindow parent, Keyframe k, ObjectInfo info) {
        new ActorEditorWindow(parent, info, this, (ActorKeyframe) k, null);
    }

    /**
     * This is a utility routine. It takes an Object3D as its argument. If the object is an
     * Actor, it is simply returned. If the argument is an ObjectWrapper which contains an Actor,
     * the inner Actor is returned. Otherwise, it returns null.
     */
    public static Actor getActor(Object3D obj) {
        while (obj instanceof ObjectWrapper) {
            if (obj instanceof Actor) {
                return (Actor) obj;
            }
            obj = ((ObjectWrapper) obj).getWrappedObject();
        }
        return null;
    }

    /**
     * Inner class representing a pose for an Actor. It consists of a list of gestures, and a
     * weight for each one.
     */
    public static class ActorKeyframe implements Keyframe {

        int[] id;
        double[] weight;

        public ActorKeyframe() {
            id = new int[0];
            weight = new double[0];
        }

        public ActorKeyframe(int[] id, double[] weight) {
            this.id = id;
            this.weight = weight;
        }

        /**
         * Get the number of gestures in this keyframe.
         */
        public int getNumGestures() {
            return id.length;
        }

        /**
         * Get the ID of a gesture in this keyframe.
         */
        public int getGestureID(int index) {
            return id[index];
        }

        /**
         * Get the weight for a gesture in this keyframe.
         */
        public double getGestureWeight(int index) {
            return weight[index];
        }

        /**
         * Add a gesture to an ActorKeyframe.
         */
        public void addGesture(int addID, double addWeight) {
            var newId = new int[id.length + 1];
            var newWeight = new double[weight.length + 1];
            System.arraycopy(id, 0, newId, 0, id.length);
            System.arraycopy(weight, 0, newWeight, 0, weight.length);
            newId[id.length] = addID;
            newWeight[weight.length] = addWeight;
            id = newId;
            weight = newWeight;
        }

        /**
         * Delete a gesture from an ActorKeyframe.
         */
        public void deleteGesture(int which) {
            var newId = new int[id.length - 1];
            var newWeight = new double[weight.length - 1];
            for (int i = 0, j = 0; i < id.length; i++) {
                if (i == which) {
                    continue;
                }
                newId[j] = id[i];
                newWeight[j++] = weight[i];
            }
            id = newId;
            weight = newWeight;
        }

        /**
         * Create a duplicate of this keyframe.
         */
        @Override
        public Keyframe duplicate(Object owner) {
            return duplicate();
        }

        @Override
        public Keyframe duplicate() {
            ActorKeyframe k = new ActorKeyframe();
            k.id = new int[id.length];
            k.weight = new double[weight.length];
            System.arraycopy(id, 0, k.id, 0, id.length);
            System.arraycopy(weight, 0, k.weight, 0, weight.length);
            return k;
        }

        /**
         * Make this keyframe identical to another one.
         */
        public void copy(ActorKeyframe key) {
            id = new int[key.id.length];
            weight = new double[key.weight.length];
            System.arraycopy(key.id, 0, id, 0, id.length);
            System.arraycopy(key.weight, 0, weight, 0, weight.length);
        }

        /**
         * Add the weights from a keyframe into a hashtable.
         */
        private void addWeightsToTable(ActorKeyframe k, Map<Integer, Double> table, double scale) {
            for (int i = 0; i < k.id.length; i++) {
                Integer key = k.id[i];
                Double weight = table.get(key);
                if (weight == null) {
                    weight = k.weight[i] * scale;
                } else {
                    weight = k.weight[i] * scale + weight;
                }
                table.put(key, weight);
            }
        }

        /**
         * Create a keyframe from the information in a hashtable.
         */
        private ActorKeyframe getKeyframeFromTable(Hashtable<Integer, Double> table) {
            ActorKeyframe k = new ActorKeyframe();
            k.id = new int[table.size()];
            k.weight = new double[k.id.length];
            Enumeration<Integer> keys = table.keys();
            int j = 0;
            for (int i = 0; i < k.id.length; i++) {
                Integer key = keys.nextElement();
                Double weight = table.get(key);
                k.id[j] = key;
                k.weight[j] = weight;
                if (k.weight[j] != 0.0) {
                    j++;
                }
            }
            if (j < k.id.length) {
                int[] id = new int[j];
                double[] weight = new double[j];
                System.arraycopy(k.id, 0, id, 0, j);
                System.arraycopy(k.weight, 0, weight, 0, j);
                k.id = id;
                k.weight = weight;
            }
            return k;
        }

        /**
         * These methods return a new Keyframe which is a weighted average of this one and one,
         * two, or three others.
         */
        @Override
        public Keyframe blend(Keyframe o2, double weight1, double weight2) {
            Hashtable<Integer, Double> table = new Hashtable<>();

            addWeightsToTable(this, table, weight1);
            addWeightsToTable((ActorKeyframe) o2, table, weight2);
            return getKeyframeFromTable(table);
        }

        @Override
        public Keyframe blend(Keyframe o2, Keyframe o3, double weight1, double weight2, double weight3) {
            Hashtable<Integer, Double> table = new Hashtable<>();

            addWeightsToTable(this, table, weight1);
            addWeightsToTable((ActorKeyframe) o2, table, weight2);
            addWeightsToTable((ActorKeyframe) o3, table, weight3);
            return getKeyframeFromTable(table);
        }

        @Override
        public Keyframe blend(Keyframe o2, Keyframe o3, Keyframe o4, double weight1, double weight2, double weight3, double weight4) {
            Hashtable<Integer, Double> table = new Hashtable<>();

            addWeightsToTable(this, table, weight1);
            addWeightsToTable((ActorKeyframe) o2, table, weight2);
            addWeightsToTable((ActorKeyframe) o3, table, weight3);
            addWeightsToTable((ActorKeyframe) o4, table, weight4);
            return getKeyframeFromTable(table);
        }

        /**
         * Determine whether this keyframe is identical to another one.
         */
        @Override
        public boolean equals(Keyframe k) {
            if (!(k instanceof ActorKeyframe)) {
                return false;
            }
            ActorKeyframe key = (ActorKeyframe) k;
            if (id.length != key.id.length) {
                return false;
            }
            for (int i = 0; i < id.length; i++) {
                if (id[i] != key.id[i] || weight[i] != key.weight[i]) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Create a keyframe for the Actor's "inner" object, based on this keyframes list of poses.
         */
        public Keyframe createObjectKeyframe(Actor actor) {
            List<Gesture> poseVec = new Vector<>();
            List<Double> weightVec = new Vector<>();

            for (int i = 0; i < id.length; i++) {
                int which = actor.findPoseIndex(id[i]);
                if (which > -1) {
                    poseVec.add(actor.gesture[which]);
                    weightVec.add(weight[i]);
                }
            }
            Gesture[] blendPose = new Gesture[poseVec.size()];
            double[] blendWeight = new double[poseVec.size()];
            for (int i = 0; i < blendPose.length; i++) {
                blendPose[i] = poseVec.get(i);
                blendWeight[i] = weightVec.get(i);
            }
            return actor.gesture[0].blend(blendPose, blendWeight);
        }

        /**
         * Write out a representation of this keyframe to a stream.
         */
        @Override
        public void writeToStream(DataOutputStream out) throws IOException {
            out.writeInt(id.length);
            for (int i = 0; i < id.length; i++) {
                out.writeInt(id[i]);
                out.writeDouble(weight[i]);
            }
        }

        /**
         * Reconstructs the keyframe from its serialized representation.
         */
        public ActorKeyframe(DataInputStream in, Object parent) throws IOException {
            int num = in.readInt();

            id = new int[num];
            weight = new double[num];
            for (int i = 0; i < num; i++) {
                id[i] = in.readInt();
                weight[i] = in.readDouble();
            }
        }
    }
}
