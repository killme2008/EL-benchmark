import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;


/**
 * EL benchmark
 * 
 * @author boyan
 * @Date 2011-7-13
 * 
 */
public class Benchmark {
    static int count = 10000000;


    public static void main(String args[]) throws Exception {
        // AviatorEvaluator.setOptimize(AviatorEvaluator.COMPILE);
        // AviatorEvaluator.setTrace(true);
        // AviatorEvaluator.setTraceOutputStream(new FileOutputStream(new
        // File("aviator.log")));
        testCompile();
        testLiteral();
        testVariableExpression();
        testFunction();
    }


    public static void testLiteral() throws Exception {
        System.out.println("test literal arith expression ...");
        benchmark("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71");
        benchmark("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3");
    }


    public static void testVariableExpression() throws Exception {
        System.out.println("test including variable expression ...");
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        byte b = (byte) 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        benchmark(
            "i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99 ==i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99",
            env);
        benchmark("pi*d+b-(1000-d*b/pi)/(pi+99-i*d)-i*pi*d/b", env);

    }


    public static void testCompile() throws Exception {
        System.out.println("test compile....");
        String exp =
                "i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99 ==i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99";
        testCompileAviator(exp);
        testCompileGroovy(exp);
        testCompileJEXL(exp);
    }


    public static void testFunction() throws Exception {
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        Map<String, Object> env2 = new HashMap<String, Object>();
        env.put("a", 1);
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("c", env2);
        env1.put("d", 5);
        env2.put("e", 4);

        final String exp2 = " new java.util.Date()";
        System.out.println("expression:" + exp2);
        testAviator("sysdate()", env);
        testGroovy(exp2, env);
        testJEXL("new(\"java.util.Date\")", env);

        final String exp1 = "s.substring(b.d)";
        System.out.println("expression:" + exp1);
        testAviator("string.substring(s,b.d)", env);
        testGroovy(exp1, env);
        testJEXL(exp1, env);

        final String exp3 = "s.substring(b.d).substring(a,b.c.e)";
        System.out.println("expression:" + exp3);
        testAviator("string.substring(string.substring(s,b.d),a,b.c.e)", env);
        testGroovy(exp3, env);
        testJEXL(exp3, env);

    }


    private static void benchmark(String exp) throws Exception {
        System.out.println("expression:" + exp);
        testAviator(exp);
        testGroovy(exp);
        testJEXL(exp);
    }


    private static void benchmark(String exp, Map<String, Object> env) throws Exception {
        System.out.println("expression:" + exp);
        testAviator(exp, env);
        testGroovy(exp, env);
        testJEXL(exp, env);
    }


    private static void testAviator(String exp, Map<String, Object> env) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = expression.execute(env);
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testCompileAviator(String exp) throws Exception {
        long start = System.currentTimeMillis();
        Expression expression = null;
        for (int i = 0; i < count / 10000; i++) {
            expression = AviatorEvaluator.compile(exp);
        }
        System.out.println(expression + " compile aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testAviator(String exp) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = expression.execute();
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testCompileGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        GroovyObject groovyObject = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count / 10000; i++) {
            Class clazz = loader.parseClass(exp);
            groovyObject = (GroovyObject) clazz.newInstance();
        }

        System.out.println(groovyObject + " compile groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp, Map<String, Object> env) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testJEXL(String exp) throws Exception {
        JexlEngine jexl = new JexlEngine();

        Object result = null;
        final org.apache.commons.jexl2.Expression e = jexl.createExpression(exp);
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = e.evaluate(null);
        }

        System.out.println(result + ",jexl duration:" + (System.currentTimeMillis() - start));
    }


    public static void testJEXL(String exp, Map<String, Object> env) throws Exception {
        JexlEngine jexl = new JexlEngine();
        JexlContext context = new MapContext();
        for (Map.Entry<String, Object> entry : env.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        Object result = null;
        final org.apache.commons.jexl2.Expression e = jexl.createExpression(exp);
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = e.evaluate(context);
        }

        System.out.println(result + ",jexl duration:" + (System.currentTimeMillis() - start));
    }


    public static void testCompileJEXL(String exp) throws Exception {
        JexlEngine jexl = new JexlEngine();
        org.apache.commons.jexl2.Expression result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count / 10000; i++) {
            result = jexl.createExpression(exp);
        }

        System.out.println(result + " compile jexl duration:" + (System.currentTimeMillis() - start));
    }

}
