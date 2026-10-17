import java.util.*;

/** Seven-day scheduler with ranked preferences and capacity constraints. */
public class EmployeeScheduler {
    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    static final String[] SHIFTS = {"Morning", "Afternoon", "Evening"};
    static final String[] NAMES = {"Alex", "Blair", "Casey", "Drew", "Emery", "Finley", "Gray", "Harper", "Indigo", "Jordan", "Kai", "Logan"};
    static final int MIN_STAFF = 2, MAX_DAYS = 5;

    static Map<String, Map<String, List<String>>> preferences() {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (int i = 0; i < NAMES.length; i++) {
            Map<String, List<String>> perDay = new LinkedHashMap<>();
            for (int j = 0; j < DAYS.length; j++) {
                List<String> ranked = new ArrayList<>();
                for (int k = 0; k < SHIFTS.length; k++) ranked.add(SHIFTS[(i+j+k)%3]);
                perDay.put(DAYS[j], ranked);
            }
            result.put(NAMES[i], perDay);
        }
        return result;
    }

    public static void main(String[] args) {
        if (NAMES.length * MAX_DAYS < DAYS.length * SHIFTS.length * MIN_STAFF)
            throw new IllegalStateException("Insufficient weekly employee capacity");
        Map<String, Map<String, List<String>>> prefs = preferences();
        Map<String, Integer> weekly = new LinkedHashMap<>();
        for (String name : NAMES) weekly.put(name, 0);
        Random random = new Random(42);
        System.out.println("EMPLOYEE WEEKLY SHIFT SCHEDULE");
        System.out.println("========================================================================");
        for (String day : DAYS) {
            Map<String, List<String>> assigned = new LinkedHashMap<>();
            for (String shift : SHIFTS) assigned.put(shift, new ArrayList<>());
            List<String> available = new ArrayList<>();
            for (String name : NAMES) if (weekly.get(name) < MAX_DAYS) available.add(name);
            if (available.size() < SHIFTS.length * MIN_STAFF)
                throw new IllegalStateException("Insufficient staff on " + day);
            Collections.shuffle(available, random);
            available.sort(Comparator.comparingInt(weekly::get));
            for (String name : available) {
                // A full first-choice shift triggers a same-day alternative.
                for (String shift : prefs.get(name).get(day)) {
                    if (assigned.get(shift).size() < MIN_STAFF) {
                        assigned.get(shift).add(name);
                        weekly.put(name, weekly.get(name) + 1);
                        break;
                    }
                }
                boolean full = true;
                for (String shift : SHIFTS) if (assigned.get(shift).size() < MIN_STAFF) full = false;
                if (full) break;
            }
            Set<String> unique = new HashSet<>();
            System.out.println("\n" + day);
            for (String shift : SHIFTS) {
                List<String> workers = assigned.get(shift);
                if (workers.size() < MIN_STAFF) throw new IllegalStateException("Understaffed: " + day + " " + shift);
                for (String name : workers) if (!unique.add(name)) throw new IllegalStateException("Duplicate daily assignment");
                System.out.printf("  %-10s: %s%n", shift, String.join(", ", workers));
            }
        }
        for (int count : weekly.values()) if (count > MAX_DAYS) throw new IllegalStateException("Weekly limit exceeded");
        System.out.print("\nDAYS WORKED: ");
        List<String> totals = new ArrayList<>();
        for (Map.Entry<String,Integer> e : weekly.entrySet()) totals.add(e.getKey() + "=" + e.getValue());
        System.out.println(String.join(", ", totals));
        System.out.println("VALIDATION PASSED: 2 per shift; max 1 shift/day; max 5 days/week.");
    }
}
