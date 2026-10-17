"""Seven-day employee scheduler: preferences, daily/weekly limits, conflict fallback."""
import random

DAYS = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
SHIFTS = ['Morning','Afternoon','Evening']
NAMES = ['Alex','Blair','Casey','Drew','Emery','Finley','Gray','Harper','Indigo','Jordan','Kai','Logan']
MIN_STAFF = 2
MAX_DAYS = 5
SEED = 42

def preferences():
    """Input data structure: ranked preferred shifts for every employee and day."""
    return {name: {day: [SHIFTS[(i+j+k)%3] for k in range(3)]
                         for j,day in enumerate(DAYS)} for i,name in enumerate(NAMES)}

def schedule_week(names=NAMES, prefs=None, seed=SEED):
    if len(set(names)) != len(names):
        raise ValueError('Employee names must be unique')
    if len(names)*MAX_DAYS < len(DAYS)*len(SHIFTS)*MIN_STAFF:
        raise ValueError('Insufficient employees to meet weekly coverage')
    prefs = prefs or preferences()
    rng = random.Random(seed)
    weekly = {n: 0 for n in names}
    schedule = {}
    for day in DAYS:
        shifts = {s: [] for s in SHIFTS}
        available = [n for n in names if weekly[n] < MAX_DAYS]
        if len(available) < len(SHIFTS)*MIN_STAFF:
            raise ValueError(f'Cannot staff {day}: insufficient eligible employees')
        # Randomize ties, then prioritize employees with the most remaining capacity.
        rng.shuffle(available)
        available.sort(key=lambda n: weekly[n])
        for name in available:
            # Preferred shift first; if full, try another shift on the same day.
            ranked = prefs[name][day]
            for shift in ranked:
                if len(shifts[shift]) < MIN_STAFF:
                    shifts[shift].append(name)
                    weekly[name] += 1
                    break
            if all(len(shifts[s]) == MIN_STAFF for s in SHIFTS):
                break
        if any(len(shifts[s]) < MIN_STAFF for s in SHIFTS):
            raise ValueError(f'Coverage unmet on {day}')
        schedule[day] = shifts
    return schedule, weekly

def validate(schedule, weekly):
    assert all(count <= MAX_DAYS for count in weekly.values())
    for day in DAYS:
        assignments = [n for shift in SHIFTS for n in schedule[day][shift]]
        assert len(assignments) == len(set(assignments))
        assert all(len(schedule[day][shift]) >= MIN_STAFF for shift in SHIFTS)

def main():
    schedule, weekly = schedule_week()
    validate(schedule, weekly)
    print('EMPLOYEE WEEKLY SHIFT SCHEDULE')
    print('='*72)
    for day in DAYS:
        print(f'\n{day}')
        for shift in SHIFTS:
            print(f'  {shift:<10}: {", ".join(schedule[day][shift])}')
    print('\nDAYS WORKED: ' + ', '.join(f'{n}={c}' for n,c in weekly.items()))
    print('VALIDATION PASSED: 2 per shift; max 1 shift/day; max 5 days/week.')

if __name__ == '__main__':
    main()
