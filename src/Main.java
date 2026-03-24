import java.util.*;

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }
}

class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();

    public RoomInventory() {
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);

        counters.put("Single", 1);
        counters.put("Double", 1);
        counters.put("Suite", 1);
    }

    public String allocateRoom(String roomType) {
        int available = inventory.getOrDefault(roomType, 0);

        if (available <= 0) {
            return null;
        }

        int count = counters.get(roomType);
        String roomId = roomType + "-" + count;

        counters.put(roomType, count + 1);
        inventory.put(roomType, available - 1);

        return roomId;
    }

    public void displayInventory() {
        System.out.println("Remaining Inventory:");
        for (String type : inventory.keySet()) {
            System.out.println(type + ": " + inventory.get(type));
        }
    }
}

class RoomAllocationService {

    public void allocateRoom(Reservation r, RoomInventory inventory) {
        String roomId = inventory.allocateRoom(r.getRoomType());

        if (roomId != null) {
            System.out.println("Booking confirmed for Guest: " +
                    r.getGuestName() + ", Room ID: " + roomId);
        } else {
            System.out.println("No rooms available for " + r.getRoomType());
        }
    }
}

class ConcurrentBookingProcessor implements Runnable {

    private BookingRequestQueue bookingQueue;
    private RoomInventory inventory;
    private RoomAllocationService allocationService;

    public ConcurrentBookingProcessor(
            BookingRequestQueue bookingQueue,
            RoomInventory inventory,
            RoomAllocationService allocationService
    ) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocationService = allocationService;
    }

    @Override
    public void run() {
        while (true) {
            Reservation reservation;

            synchronized (bookingQueue) {
                reservation = bookingQueue.getNextRequest();
            }

            if (reservation == null) {
                break;
            }

            synchronized (inventory) {
                allocationService.allocateRoom(reservation, inventory);
            }
        }
    }
}

public class Main {

    public static void main(String[] args) {

        System.out.println("Concurrent Booking Simulation");

        BookingRequestQueue bookingQueue = new BookingRequestQueue();
        RoomInventory inventory = new RoomInventory();
        RoomAllocationService allocationService = new RoomAllocationService();

        bookingQueue.addRequest(new Reservation("Abhi", "Single"));
        bookingQueue.addRequest(new Reservation("Vanmathi", "Double"));
        bookingQueue.addRequest(new Reservation("Kural", "Suite"));
        bookingQueue.addRequest(new Reservation("Subha", "Single"));

        Thread t1 = new Thread(
                new ConcurrentBookingProcessor(bookingQueue, inventory, allocationService)
        );

        Thread t2 = new Thread(
                new ConcurrentBookingProcessor(bookingQueue, inventory, allocationService)
        );

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted.");
        }

        inventory.displayInventory();
    }
}