# Information System of Research-Oriented University

Console-based Java application — OOP final project (3-person team).

## Build & run

```bash
# Compile everything
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Run the demo
java -cp out com.uni.UniversityDemo
```

## Structure

```
src/com/uni/
├── enums/         Language, LessonType, TeacherTitle, ManagerType,
│                  CourseType, UrgencyLevel, CitationFormat, UserType
├── exceptions/    LowHIndexException, NotAResearcherException,
│                  CreditLimitException, MaxFailedReachedException
├── models/        User, Employee, Teacher, Manager, Admin,
│                  ResearcherEmployee, Student, Course, Mark, Lesson,
│                  Researcher, ResearcherDecorator, ResearchPaper,
│                  ResearchProject, Message, UserAction
├── comparators/   DateComparator, CitationsComparator, PaperLengthComparator
├── factory/       UserFactory
├── storage/       DataStore (Singleton, persists to data.ser)
└── UniversityDemo.java
```

## Patterns

1. **Singleton** — `DataStore`
2. **Factory** — `UserFactory`
3. **Decorator** — `ResearcherDecorator`
4. **Strategy** — paper `Comparator`s

## Generate javadoc

```bash
javadoc -d docs -sourcepath src -subpackages com.uni
```
