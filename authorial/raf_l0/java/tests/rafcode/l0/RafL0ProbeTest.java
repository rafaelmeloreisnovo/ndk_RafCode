package rafcode.l0;

/** Hosted-only deterministic fixtures; neither real APKs nor physical ART. */
public final class RafL0ProbeTest {
    private static int assertions;
    private static void check(boolean pass) {
        assertions++;
        if (!pass) throw new AssertionError("probe assertion " + assertions);
    }
    private static void u16(byte[] a, int p, int v, boolean little) {
        a[p+(little?0:1)] = (byte)v;
        a[p+(little?1:0)] = (byte)(v>>>8);
    }
    private static void u32(byte[] a, int p, long v, boolean little) {
        for (int i=0; i<4; i++) a[p+(little?i:3-i)]=(byte)(v >>> (i*8));
    }
    private static void u64(byte[] a, int p, long v, boolean little) {
        for (int i=0; i<8; i++) a[p+(little?i:7-i)]=(byte)(v >>> (i*8));
    }
    private static byte[] dex(int v) {
        byte[] a=new byte[128];
        a[0]='d'; a[1]='e'; a[2]='x'; a[3]=10;
        a[4]='0'; a[5]=(byte)('0'+v/10); a[6]=(byte)('0'+v%10); a[7]=0;
        u32(a,32,128,true);u32(a,36,112,true);u32(a,40,0x12345678L,true);
        u32(a,52,112,true);
        return a;
    }
    private static byte[] elf(int cls, boolean le) {
        byte[] a=new byte[256];
        a[0]=127;a[1]='E';a[2]='L';a[3]='F';a[4]=(byte)cls;
        a[5]=(byte)(le?1:2);a[6]=1;
        u16(a,18,cls==1?40:183,le);
        int size=cls==1?52:64,phsize=cls==1?32:56,base=cls==1?40:52;
        if(cls==1){u32(a,28,64,le);u32(a,32,0,le);}else{u64(a,32,64,le);u64(a,40,0,le);}
        u16(a,base,size,le);u16(a,base+2,phsize,le);u16(a,base+4,2,le);
        return a;
    }
    public static void main(String[] args) {
        int[] out={-777,-777,-777,-777,-777,-777,-777,-777};
        for(int version: new int[]{35,37,38,39,40}) {
            byte[] d=dex(version);
            check(RafL0.probeDex(d,0,d.length,out,1)==RafL0.OK);
            check(out[1]==version && out[2]==128 && out[3]==112 && out[0]==-777 && out[4]==-777);
            check(RafL0.probeDex(d,0,111,out,1)==RafL0.SHORT_BUFFER);
            byte[] embedded=new byte[d.length+8];
            System.arraycopy(d,0,embedded,4,d.length);
            check(RafL0.probeDex(embedded,4,d.length,out,1)==RafL0.OK);
        }
        check(RafL0.probeDex(dex(36),0,128,out,1)==RafL0.UNSUPPORTED);
        check(RafL0.probeDex(dex(41),0,128,out,1)==RafL0.UNSUPPORTED);
        byte[] d=dex(39);
        d[0]=0;check(RafL0.probeDex(d,0,128,out,1)==RafL0.MALFORMED);d[0]='d';
        u32(d,32,129,true);check(RafL0.probeDex(d,0,128,out,1)==RafL0.MALFORMED);
        u32(d,32,128,true);u32(d,36,111,true);check(RafL0.probeDex(d,0,128,out,1)==RafL0.MALFORMED);
        u32(d,36,112,true);u32(d,40,0x78563412L,true);check(RafL0.probeDex(d,0,128,out,1)==RafL0.MALFORMED);
        u32(d,40,0x12345678,true);u32(d,52,115,true);check(RafL0.probeDex(d,0,128,out,1)==RafL0.MALFORMED);
        check(out[1]==40); // previous successful DEX 040 value retained after every failure
        check(RafL0.probeDex(null,0,0,out,1)==RafL0.BAD_ARG);
        check(RafL0.probeDex(d,-1,128,out,1)==RafL0.BAD_ARG);
        check(RafL0.probeDex(d,Integer.MAX_VALUE,1,out,1)==RafL0.SHORT_BUFFER);
        check(RafL0.probeDex(d,0,128,out,6)==RafL0.BAD_ARG);
        check(RafL0.probeDex(d,0,128,null,0)==RafL0.BAD_ARG);
        for(int cls=1; cls<=2; cls++)for(int l=0;l<2;l++){
            boolean le=l==0;byte[] e=elf(cls,le);
            check(RafL0.probeElf(e,0,e.length,out,1)==RafL0.OK);
            check(out[1]==cls&&out[2]==(le?1:2)&&out[3]==(cls==1?40:183)&&out[4]==2&&out[5]==0);
            check(RafL0.probeElf(e,0,(cls==1?51:63),out,1)==RafL0.SHORT_BUFFER);
            int base=cls==1?40:52;
            u16(e,base,65,le);check(RafL0.probeElf(e,0,e.length,out,1)==RafL0.MALFORMED);
            u16(e,base,cls==1?52:64,le);
            u16(e,base+4,100,le);check(RafL0.probeElf(e,0,e.length,out,1)==RafL0.MALFORMED);
            u16(e,base+4,2,le);check(RafL0.probeElf(e,0,e.length,out,1)==RafL0.OK);
            byte[] pad=new byte[280];System.arraycopy(e,0,pad,7,e.length);
            check(RafL0.probeElf(pad,7,e.length,out,1)==RafL0.OK);
        }
        byte[] e=elf(2,true);e[4]=3;
        check(RafL0.probeElf(e,0,256,out,1)==RafL0.UNSUPPORTED);
        e=elf(2,true);u64(e,32,0x100000000L,true);
        check(RafL0.probeElf(e,0,256,out,1)==RafL0.MALFORMED);
        e=elf(1,true);e[0]=0;
        check(RafL0.probeElf(e,0,256,out,1)==RafL0.MALFORMED);
        check(RafL0.probeElf(null,0,0,out,1)==RafL0.BAD_ARG);
        check(RafL0.probeElf(e,0,256,out,4)==RafL0.BAD_ARG);
        check(RafL0.probeElf(e,Integer.MAX_VALUE,1,out,1)==RafL0.SHORT_BUFFER);
        int rng=0x5eedd3;
        for(int z=0;z<8192;z++) {
            byte[] f=new byte[(z%143)+1];
            for(int i=0;i<f.length;i++){rng=rng*1664525+1013904223;f[i]=(byte)(rng>>>24);}
            int safe=out[1];
            int a=RafL0.probeElf(f,0,f.length,out,1);
            int b=RafL0.probeDex(f,0,f.length,out,1);
            check((a==RafL0.MALFORMED||a==RafL0.SHORT_BUFFER||a==RafL0.UNSUPPORTED) &&
                  (b==RafL0.MALFORMED||b==RafL0.SHORT_BUFFER||b==RafL0.UNSUPPORTED));
            check(out[1]==safe);
        }
        System.out.println("PASS Java L0 ELF/DEX probe assertions="+assertions);
    }
}
