package forge;

public class Debugger {
    static boolean enable = true;
    public static boolean isEnabled(){
        return enable;
    }
    public static void log(Object o){
        if(Debugger.isEnabled()) {
            System.out.println(o.toString());
        }
    }
}
