package se.ju.student.android_mjecipes.MjepicesAPIHandler.Entities;

public class Recipe {
    public int id;
    public String name;
    public String description;
    public String image;
    public long created;
    public Account creator;
    public String creatorId;
    public Direction[] directions;
}
