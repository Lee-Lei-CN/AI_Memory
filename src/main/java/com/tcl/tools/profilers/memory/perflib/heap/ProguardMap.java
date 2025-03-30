package com.tcl.tools.profilers.memory.perflib.heap;


import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ProguardMap {
    private static final String ARRAY_SYMBOL = "[]";
    private Map<String, ProguardMap.ClassData> mClassesFromClearName = new HashMap();
    private Map<String, ProguardMap.ClassData> mClassesFromObfuscatedName = new HashMap();

    public ProguardMap() {
    }

    private static void parseException(String msg) throws ParseException {
        throw new ParseException(msg, 0);
    }

    public void readFromFile(File mapFile) throws FileNotFoundException, IOException, ParseException {
        this.readFromReader(new FileReader(mapFile));
    }

    public void readFromReader(Reader mapReader) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(mapReader);
        String line = reader.readLine();

        while(true) {
            label50:
            while(line != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    int sep = line.indexOf(" -> ");
                    if (sep == -1 || sep + 5 >= line.length()) {
                        parseException("Error parsing class line: '" + line + "'");
                    }

                    String clearClassName = line.substring(0, sep);
                    String obfuscatedClassName = line.substring(sep + 4, line.length() - 1);
                    ProguardMap.ClassData classData = new ProguardMap.ClassData(clearClassName);
                    this.mClassesFromClearName.put(clearClassName, classData);
                    this.mClassesFromObfuscatedName.put(obfuscatedClassName, classData);
                    line = reader.readLine();

                    while(true) {
                        while(true) {
                            if (line == null) {
                                continue label50;
                            }

                            trimmed = line.trim();
                            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                                if (!line.startsWith("    ")) {
                                    continue label50;
                                }

                                int ws = trimmed.indexOf(32);
                                sep = trimmed.indexOf(" -> ");
                                if (ws == -1 || sep == -1) {
                                    parseException("Error parse field/method line: '" + line + "'");
                                }

                                String type = trimmed.substring(0, ws);
                                String clearName = trimmed.substring(ws + 1, sep);
                                String obfuscatedName = trimmed.substring(sep + 4, trimmed.length());
                                if (clearName.indexOf(40) == -1) {
                                    classData.addField(obfuscatedName, clearName);
                                } else {
                                    int obfuscatedLine = 0;
                                    int colon = type.indexOf(58);
                                    if (colon != -1) {
                                        obfuscatedLine = Integer.parseInt(type.substring(0, colon));
                                        type = type.substring(colon + 1);
                                    }

                                    colon = type.indexOf(58);
                                    if (colon != -1) {
                                        type = type.substring(colon + 1);
                                    }

                                    int op = clearName.indexOf(40);
                                    int cp = clearName.indexOf(41);
                                    if (op == -1 || cp == -1) {
                                        parseException("Error parse method line: '" + line + "'");
                                    }

                                    String sig = clearName.substring(op, cp + 1);
                                    int clearLine = obfuscatedLine;
                                    colon = clearName.lastIndexOf(58);
                                    if (colon != -1) {
                                        clearLine = Integer.parseInt(clearName.substring(colon + 1));
                                        clearName = clearName.substring(0, colon);
                                    }

                                    colon = clearName.lastIndexOf(58);
                                    if (colon != -1) {
                                        clearLine = Integer.parseInt(clearName.substring(colon + 1));
                                        clearName = clearName.substring(0, colon);
                                    }

                                    clearName = clearName.substring(0, op);
                                    String clearSig = fromProguardSignature(sig + type);
                                    classData.addFrame(obfuscatedName, clearName, clearSig, obfuscatedLine, clearLine);
                                }

                                line = reader.readLine();
                            } else {
                                line = reader.readLine();
                            }
                        }
                    }
                } else {
                    line = reader.readLine();
                }
            }

            reader.close();
            return;
        }
    }

    public String getClassName(String obfuscatedClassName) {
        String baseName = obfuscatedClassName;

        String arraySuffix;
        for(arraySuffix = ""; baseName.endsWith("[]"); baseName = baseName.substring(0, baseName.length() - "[]".length())) {
            arraySuffix = arraySuffix + "[]";
        }

        ProguardMap.ClassData classData = (ProguardMap.ClassData)this.mClassesFromObfuscatedName.get(baseName);
        String clearBaseName = classData == null ? baseName : classData.getClearName();
        return clearBaseName + arraySuffix;
    }

    public String getFieldName(String clearClass, String obfuscatedField) {
        ProguardMap.ClassData classData = (ProguardMap.ClassData)this.mClassesFromClearName.get(clearClass);
        return classData == null ? obfuscatedField : classData.getField(obfuscatedField);
    }

    public ProguardMap.Frame getFrame(String clearClassName, String obfuscatedMethodName, String obfuscatedSignature, String obfuscatedFilename, int obfuscatedLine) {
        String clearSignature = this.getSignature(obfuscatedSignature);
        ProguardMap.ClassData classData = (ProguardMap.ClassData)this.mClassesFromClearName.get(clearClassName);
        return classData == null ? new ProguardMap.Frame(obfuscatedMethodName, clearSignature, obfuscatedFilename, obfuscatedLine) : classData.getFrame(clearClassName, obfuscatedMethodName, clearSignature, obfuscatedFilename, obfuscatedLine);
    }

    private static String fromProguardSignature(String sig) throws ParseException {
        if (sig.startsWith("(")) {
            int end = sig.indexOf(41);
            if (end == -1) {
                parseException("Error parsing signature: " + sig);
            }

            StringBuilder converted = new StringBuilder();
            converted.append('(');
            if (end > 1) {
                String[] var3 = sig.substring(1, end).split(",");
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String arg = var3[var5];
                    converted.append(fromProguardSignature(arg));
                }
            }

            converted.append(')');
            converted.append(fromProguardSignature(sig.substring(end + 1)));
            return converted.toString();
        } else if (sig.endsWith("[]")) {
            return "[" + fromProguardSignature(sig.substring(0, sig.length() - 2));
        } else if (sig.equals("boolean")) {
            return "Z";
        } else if (sig.equals("byte")) {
            return "B";
        } else if (sig.equals("char")) {
            return "C";
        } else if (sig.equals("short")) {
            return "S";
        } else if (sig.equals("int")) {
            return "I";
        } else if (sig.equals("long")) {
            return "J";
        } else if (sig.equals("float")) {
            return "F";
        } else if (sig.equals("double")) {
            return "D";
        } else {
            return sig.equals("void") ? "V" : "L" + sig.replace('.', '/') + ";";
        }
    }

    private String getSignature(String obfuscatedSig) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < obfuscatedSig.length(); ++i) {
            if (obfuscatedSig.charAt(i) == 'L') {
                int e = obfuscatedSig.indexOf(59, i);
                builder.append('L');
                String cls = obfuscatedSig.substring(i + 1, e).replace('/', '.');
                builder.append(this.getClassName(cls).replace('.', '/'));
                builder.append(';');
                i = e;
            } else {
                builder.append(obfuscatedSig.charAt(i));
            }
        }

        return builder.toString();
    }

    private static String getFileName(String clearClass) {
        String filename = clearClass;
        int dot = clearClass.lastIndexOf(46);
        if (dot != -1) {
            filename = clearClass.substring(dot + 1);
        }

        int dollar = filename.indexOf(36);
        if (dollar != -1) {
            filename = filename.substring(0, dollar);
        }

        return filename + ".java";
    }

    public static class Frame {
        public final String methodName;
        public final String signature;
        public final String filename;
        public final int line;

        public Frame(String methodName, String signature, String filename, int line) {
            this.methodName = methodName;
            this.signature = signature;
            this.filename = filename;
            this.line = line;
        }
    }

    private static class ClassData {
        private String mClearName;
        private Map<String, String> mFields = new HashMap();
        private Map<String, ProguardMap.FrameData> mFrames = new HashMap();

        public ClassData(String clearName) {
            this.mClearName = clearName;
        }

        public String getClearName() {
            return this.mClearName;
        }

        public void addField(String obfuscatedName, String clearName) {
            this.mFields.put(obfuscatedName, clearName);
        }

        public String getField(String obfuscatedName) {
            String clearField = (String)this.mFields.get(obfuscatedName);
            return clearField == null ? obfuscatedName : clearField;
        }

        public void addFrame(String obfuscatedMethodName, String clearMethodName, String clearSignature, int obfuscatedLine, int clearLine) {
            String key = obfuscatedMethodName + clearSignature;
            this.mFrames.put(key, new ProguardMap.FrameData(clearMethodName, obfuscatedLine - clearLine));
        }

        public ProguardMap.Frame getFrame(String clearClassName, String obfuscatedMethodName, String clearSignature, String obfuscatedFilename, int obfuscatedLine) {
            String key = obfuscatedMethodName + clearSignature;
            ProguardMap.FrameData frame = (ProguardMap.FrameData)this.mFrames.get(key);
            if (frame == null) {
                frame = new ProguardMap.FrameData(obfuscatedMethodName, 0);
            }

            return new ProguardMap.Frame(frame.clearMethodName, clearSignature, ProguardMap.getFileName(clearClassName), obfuscatedLine - frame.lineDelta);
        }
    }

    private static class FrameData {
        public String clearMethodName;
        public int lineDelta;

        public FrameData(String clearMethodName, int lineDelta) {
            this.clearMethodName = clearMethodName;
            this.lineDelta = lineDelta;
        }
    }
}
