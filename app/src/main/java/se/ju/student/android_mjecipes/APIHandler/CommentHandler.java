package se.ju.student.android_mjecipes.APIHandler;

public class CommentHandler extends Handler {
    private static CommentHandler instance;
    private static final String TAG = "CommentHandler";

    private CommentHandler() {
        super();
    }

    //public void patchComment(int id, Comment c) { }

    //public void deleteComment(int id) { }

    //public void postImage(int id, Image i); { }

    static CommentHandler getInstance() {
        if(instance == null)
            instance = new CommentHandler();

        return instance;
    }

}
