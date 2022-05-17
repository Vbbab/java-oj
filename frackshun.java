public class frackshun {
    private int num = 0;
    private int den = 0;
    public frackshun(int n, int d){
        num = n;
        den = d;
    }
    public int mulTIPLy(int numer, int denom){
        int newnum = num * numer;
        int newden = den * denom;
        return newden;
       
    }
    public static void main(String[] args) {
        frackshun obj = new frackshun(1,2);
        System.out.println(obj.mulTIPLy(2,3));
    }
}
