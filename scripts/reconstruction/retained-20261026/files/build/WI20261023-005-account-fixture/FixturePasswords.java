import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Pure retained-library BCrypt helper; no Spring context, database, or network. */
public final class FixturePasswords {
    static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);
    static String read(DataInputStream in) throws Exception {
        int size=in.readInt();
        if(size<12 || size>72)throw new IllegalArgumentException();
        byte[] bytes=in.readNBytes(size);
        if(bytes.length!=size)throw new IllegalArgumentException();
        for(byte b:bytes)if(b<33 || b>126)throw new IllegalArgumentException();
        try{return new String(bytes,StandardCharsets.US_ASCII);}
        finally{Arrays.fill(bytes,(byte)0);}
    }
    public static void main(String[] args) {
        try {
            if(Runtime.version().feature()!=17)throw new IllegalArgumentException();
            if(args.length==1 && args[0].equals("self-test")) {
                String fake="Synthetic_ONLY_123!";
                String encoded=ENCODER.encode(fake);
                if(!encoded.matches("\\$2a\\$10\\$[./A-Za-z0-9]{53}") || !ENCODER.matches(fake,encoded)
                    || ENCODER.matches("Different_ONLY_456!",encoded))throw new IllegalArgumentException();
                System.out.println("{\"status\":\"BCRYPT10_RETAINED_LIBRARY_PASS\",\"secrets_output\":false}");
                return;
            }
            if(args.length!=0)throw new IllegalArgumentException();
            try(DataInputStream in=new DataInputStream(System.in)) {
                String first=read(in),second=read(in);
                if(in.read()!=-1)throw new IllegalArgumentException();
                String a=ENCODER.encode(first),b=ENCODER.encode(second);
                if(!ENCODER.matches(first,a) || !ENCODER.matches(second,b))throw new IllegalArgumentException();
                System.out.println(a);System.out.println(b);
            }
        } catch(Throwable ignored) { System.err.println("BCRYPT_HOLD");System.exit(2); }
    }
}
