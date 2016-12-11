package se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities;

import java.io.Serializable;

public class Recipe implements Serializable {
    public int id;
    public String name;
    public String description;
    public String image;
    public long created;
    public Account creator;
    public String creatorId;
    public Direction[] directions;
}
