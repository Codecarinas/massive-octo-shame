package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.ObjectUtils;

public class DataWatcher {

    private final Entity a;
    private boolean b = true;
    private static final Map c = Maps.newHashMap();
    private final Map d = Maps.newHashMap();
    private boolean e;
    private ReadWriteLock f = new ReentrantReadWriteLock();

    public DataWatcher(Entity entity) {
        this.a = entity;
    }

    public void a(int i, Object object) {
        Integer integer = (Integer) DataWatcher.c.get(object.getClass());

        if (integer == null) {
            throw new IllegalArgumentException("Unknown data type: " + object.getClass());
        } else if (i > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 31 + ")");
        } else if (this.d.containsKey(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else {
            WatchableObject watchableobject = new WatchableObject(integer.intValue(), i, object);

            this.f.writeLock().lock();
            this.d.put(Integer.valueOf(i), watchableobject);
            this.f.writeLock().unlock();
            this.b = false;
        }
    }

    public void add(int i, int j) {
        WatchableObject watchableobject = new WatchableObject(j, i, (Object) null);

        this.f.writeLock().lock();
        this.d.put(Integer.valueOf(i), watchableobject);
        this.f.writeLock().unlock();
        this.b = false;
    }

    public byte getByte(int i) {
        return ((Byte) this.j(i).b()).byteValue();
    }

    public short getShort(int i) {
        return ((Short) this.j(i).b()).shortValue();
    }

    public int getInt(int i) {
        return ((Integer) this.j(i).b()).intValue();
    }

    public float getFloat(int i) {
        return ((Float) this.j(i).b()).floatValue();
    }

    public String getString(int i) {
        return (String) this.j(i).b();
    }

    public ItemStack getItemStack(int i) {
        return (ItemStack) this.j(i).b();
    }

    private WatchableObject j(int i) {
        this.f.readLock().lock();

        WatchableObject watchableobject;

        try {
            watchableobject = (WatchableObject) this.d.get(Integer.valueOf(i));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Getting synched entity data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Synched entity data");

            crashreportsystemdetails.a("Data ID", (Object) Integer.valueOf(i));
            throw new ReportedException(crashreport);
        }

        this.f.readLock().unlock();
        return watchableobject;
    }

    public Vector3f h(int i) {
        return (Vector3f) this.j(i).b();
    }

    public void watch(int i, Object object) {
        WatchableObject watchableobject = this.j(i);

        if (ObjectUtils.notEqual(object, watchableobject.b())) {
            watchableobject.a(object);
            this.a.i(i);
            watchableobject.a(true);
            this.e = true;
        }

    }

    public void update(int i) {
        WatchableObject.a(this.j(i), true);
        this.e = true;
    }

    public boolean a() {
        return this.e;
    }

    public static void a(List list, PacketDataSerializer packetdataserializer) {
        if (list != null) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                WatchableObject watchableobject = (WatchableObject) iterator.next();

                a(packetdataserializer, watchableobject);
            }
        }

        packetdataserializer.writeByte(127);
    }

    public List b() {
        ArrayList arraylist = null;

        if (this.e) {
            this.f.readLock().lock();
            Iterator iterator = this.d.values().iterator();

            while (iterator.hasNext()) {
                WatchableObject watchableobject = (WatchableObject) iterator.next();

                if (watchableobject.d()) {
                    watchableobject.a(false);
                    if (arraylist == null) {
                        arraylist = Lists.newArrayList();
                    }

                    arraylist.add(watchableobject);
                }
            }

            this.f.readLock().unlock();
        }

        this.e = false;
        return arraylist;
    }

    public void a(PacketDataSerializer packetdataserializer) {
        this.f.readLock().lock();
        Iterator iterator = this.d.values().iterator();

        while (iterator.hasNext()) {
            WatchableObject watchableobject = (WatchableObject) iterator.next();

            a(packetdataserializer, watchableobject);
        }

        this.f.readLock().unlock();
        packetdataserializer.writeByte(127);
    }

    public List c() {
        ArrayList arraylist = null;

        this.f.readLock().lock();

        WatchableObject watchableobject;

        for (Iterator iterator = this.d.values().iterator(); iterator.hasNext(); arraylist.add(watchableobject)) {
            watchableobject = (WatchableObject) iterator.next();
            if (arraylist == null) {
                arraylist = Lists.newArrayList();
            }
        }

        this.f.readLock().unlock();
        return arraylist;
    }

    private static void a(PacketDataSerializer packetdataserializer, WatchableObject watchableobject) {
        int i = (watchableobject.c() << 5 | watchableobject.a() & 31) & 255;

        packetdataserializer.writeByte(i);
        switch (watchableobject.c()) {
        case 0:
            packetdataserializer.writeByte(((Byte) watchableobject.b()).byteValue());
            break;

        case 1:
            packetdataserializer.writeShort(((Short) watchableobject.b()).shortValue());
            break;

        case 2:
            packetdataserializer.writeInt(((Integer) watchableobject.b()).intValue());
            break;

        case 3:
            packetdataserializer.writeFloat(((Float) watchableobject.b()).floatValue());
            break;

        case 4:
            packetdataserializer.a((String) watchableobject.b());
            break;

        case 5:
            ItemStack itemstack = (ItemStack) watchableobject.b();

            packetdataserializer.a(itemstack);
            break;

        case 6:
            BlockPosition blockposition = (BlockPosition) watchableobject.b();

            packetdataserializer.writeInt(blockposition.getX());
            packetdataserializer.writeInt(blockposition.getY());
            packetdataserializer.writeInt(blockposition.getZ());
            break;

        case 7:
            Vector3f vector3f = (Vector3f) watchableobject.b();

            packetdataserializer.writeFloat(vector3f.getX());
            packetdataserializer.writeFloat(vector3f.getY());
            packetdataserializer.writeFloat(vector3f.getZ());
        }

    }

    public static List b(PacketDataSerializer packetdataserializer) {
        ArrayList arraylist = null;

        for (byte b0 = packetdataserializer.readByte(); b0 != 127; b0 = packetdataserializer.readByte()) {
            if (arraylist == null) {
                arraylist = Lists.newArrayList();
            }

            int i = (b0 & 224) >> 5;
            int j = b0 & 31;
            WatchableObject watchableobject = null;

            switch (i) {
            case 0:
                watchableobject = new WatchableObject(i, j, Byte.valueOf(packetdataserializer.readByte()));
                break;

            case 1:
                watchableobject = new WatchableObject(i, j, Short.valueOf(packetdataserializer.readShort()));
                break;

            case 2:
                watchableobject = new WatchableObject(i, j, Integer.valueOf(packetdataserializer.readInt()));
                break;

            case 3:
                watchableobject = new WatchableObject(i, j, Float.valueOf(packetdataserializer.readFloat()));
                break;

            case 4:
                watchableobject = new WatchableObject(i, j, packetdataserializer.c(32767));
                break;

            case 5:
                watchableobject = new WatchableObject(i, j, packetdataserializer.i());
                break;

            case 6:
                int k = packetdataserializer.readInt();
                int l = packetdataserializer.readInt();
                int i1 = packetdataserializer.readInt();

                watchableobject = new WatchableObject(i, j, new BlockPosition(k, l, i1));
                break;

            case 7:
                float f = packetdataserializer.readFloat();
                float f1 = packetdataserializer.readFloat();
                float f2 = packetdataserializer.readFloat();

                watchableobject = new WatchableObject(i, j, new Vector3f(f, f1, f2));
            }

            arraylist.add(watchableobject);
        }

        return arraylist;
    }

    public boolean d() {
        return this.b;
    }

    public void e() {
        this.e = false;
    }

    static {
        DataWatcher.c.put(Byte.class, Integer.valueOf(0));
        DataWatcher.c.put(Short.class, Integer.valueOf(1));
        DataWatcher.c.put(Integer.class, Integer.valueOf(2));
        DataWatcher.c.put(Float.class, Integer.valueOf(3));
        DataWatcher.c.put(String.class, Integer.valueOf(4));
        DataWatcher.c.put(ItemStack.class, Integer.valueOf(5));
        DataWatcher.c.put(BlockPosition.class, Integer.valueOf(6));
        DataWatcher.c.put(Vector3f.class, Integer.valueOf(7));
    }
}
