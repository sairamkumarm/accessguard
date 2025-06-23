package logging;

public class TenantContext {
    private static final ThreadLocal<String> currentThread = new ThreadLocal<>();

    public static void setTenant(String tenantName){
        currentThread.set(tenantName);
    }

    public static String getTenant(){
        return currentThread.get();
    }

    public static void clear(){
        currentThread.remove();
    }
}
