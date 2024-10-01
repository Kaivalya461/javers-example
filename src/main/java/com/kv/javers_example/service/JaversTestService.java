package com.kv.javers_example.service;

import com.kv.javers_example.dto.MyChangeDetectionResponseModel;
import com.kv.javers_example.dto.MyChangeFieldsDto;
import com.kv.javers_example.dto.MyListChangeFieldsDto;
import com.kv.javers_example.model.Address;
import com.kv.javers_example.model.Employee;
import com.kv.javers_example.model.MyDrug;
import lombok.extern.log4j.Log4j2;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.InitialValueChange;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.container.ValueRemoved;
import org.javers.core.metamodel.object.InstanceId;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.javers.core.diff.ListCompareAlgorithm.LEVENSHTEIN_DISTANCE;

@Service
@Log4j2
public class JaversTestService {
    private Javers javers = JaversBuilder.javers()
            .withListCompareAlgorithm(LEVENSHTEIN_DISTANCE)
            .build();

    public List<MyChangeDetectionResponseModel> nestedListTest() {
        MyDrug meth = new MyDrug("68308-115-01", "Methamphetamine", 68);
        MyDrug marijuana = new MyDrug("7360", "Marijuana", 55.5);
        MyDrug cocaine = new MyDrug("64950-362-04", "Cocaine", 11);

        Employee frodoOld = Employee.builder()
                .id(1)
                .name("Frodo")
                .age(40)
                .salary(10_000)
                .primaryAddress(new Address("Shire"))
                .skills(Set.of("management"))
                .subordinates(List.of(new Employee(2, "Sam")))
                .favoriteDrugs(List.of(meth))
                .build();

//        MyDrug meth2 = new MyDrug("68308-115-01", "Methamphetamine", 66); //For testing, Existing Drug Edit
        Employee frodoNew = Employee.builder()
                .id(1)
                .name("Frodo")
                .age(40)
                .salary(10_000)
                .primaryAddress(new Address("Shire"))
                .skills(Set.of("management"))
                .subordinates(List.of(new Employee(2, "Sam")))
                .favoriteDrugs(List.of(meth, marijuana))
                .build();

        Employee gandalf = Employee.builder()
                .id(2)
                .name("Gandalf")
                .age(24_000)
                .salary(12_000)
                .primaryAddress(new Address("Mordor"))
                .skills(Set.of("agile coaching"))
                .subordinates(List.of(new Employee(2, "Sam")))
                .build();

        //assertions
//        assertThat("Diff Size mismatch", diff.getChanges().size() == 10);

        log.info("Done");

        return prepareResponseModel(List.of(frodoOld, frodoNew, gandalf));
    }

    private List<MyChangeDetectionResponseModel> prepareResponseModel(List<Employee> employeeList) {
        Map<Integer, List<Employee>> groupedData = employeeList.stream()
                .collect(Collectors.groupingBy(Employee::getId));

        List<MyChangeDetectionResponseModel> finalList = new ArrayList<>();

        groupedData
                .forEach((key, value) -> {
                    Integer empId = key;
                    List<Employee> employeeObjectHistory = value;
                    Diff diff = null;

                    if (employeeObjectHistory.size() == 1) {
                        diff = processAndFetchDiff(new Employee(), employeeObjectHistory.get(0));
                    } else if (employeeObjectHistory.size() > 1) {
                        for (int i = 0; i < employeeObjectHistory.size() - 1; i++) {
                            diff = processAndFetchDiff(employeeObjectHistory.get(i), employeeObjectHistory.get(i + 1));
                        }
                    }


                    MyChangeDetectionResponseModel model = new MyChangeDetectionResponseModel();
                    model.setEmpId(String.valueOf(empId));

                    if (diff.hasChanges()) {
                        Diff finalDiff = diff;
                        var map = Stream.of(NewObject.class, ObjectRemoved.class)
                                .flatMap(type -> finalDiff.getChangesByType(type).stream())
                                .collect(
                                        Collectors.toMap(change -> change.getAffectedLocalId().toString(),
                                                change -> change.getAffectedObject().get()));

                        //Value Change
                        model.setChangeFieldsList(processValueChangeFieldsList(diff));

                        //List Change
                        model.setFavoriteDrugsChangeFieldDto(processFavoriteDrugListChangeFields(diff, map));

                        model.setChangeFieldsSize(diff.getChanges().size());
                    }

                    finalList.add(model);
                });


        return finalList;
    }

    private Diff processAndFetchDiff(Employee obj1, Employee obj2) {
        //diff
        Diff diff = javers.compare(obj1, obj2);

        log.info("----  ---- ---> {}", diff);

        return diff;
    }

    //types of "Change" classes
//            ArrayChange
//            CollectionChange
//            ContainerChange
//            InitialValueChange
//            KeyValueChange
//            ListChange
//            MapChange
//            MultisetChange
//            ReferenceChange
//            SetChange
//            TerminalValueChange
//            ValueChange

    private List<MyChangeFieldsDto> processValueChangeFieldsList(Diff diff) {
        List<MyChangeFieldsDto> changeFieldsDtoList = new ArrayList<>();

        diff.getChanges()
                .stream()
                .filter(change -> !(change instanceof InitialValueChange))
                .forEach(change -> {
                    MyChangeFieldsDto myChangeFieldsDto = new MyChangeFieldsDto();

                    if (change instanceof ValueChange valueChange) {
                        myChangeFieldsDto.setFieldName(valueChange.getPropertyName());

                        var leftString = Objects.nonNull(valueChange.getLeft()) ? valueChange.getLeft() : "NOOL";
                        var rightString = Objects.nonNull(valueChange.getRight()) ? valueChange.getRight() : "NOOL";
                        myChangeFieldsDto.setFieldValue(leftString.toString() + " --> " + rightString.toString());

                        changeFieldsDtoList.add(myChangeFieldsDto);
                    }

                });

        return changeFieldsDtoList;
    }


    private Map<String, Object> processFavoriteDrugListChangeFields(Diff diff, Map<String, Object> newAndRemovedObjectsMap) {
        Map<String, Object> favoriteDrugChangeFieldMap = new HashMap<>();
        List<MyListChangeFieldsDto> changeFieldsDtoList = new ArrayList<>();

        diff.getChanges()
                .stream()
                .filter(change -> change instanceof ListChange)
                .map(change -> (ListChange) change)
                .map(ContainerChange::getChanges)
                .flatMap(Collection::stream)
                        .forEach(listChanges -> {
                            MyListChangeFieldsDto listChangeFieldsDto = new MyListChangeFieldsDto();

                            if(listChanges instanceof ValueAdded valueAddedChange) {    //New Object
                                listChangeFieldsDto.setListElementChangeType("Added New Drug");
                                var newlyAddedObjectFields = new MyChangeFieldsDto();
                                Object localCdoId = ((InstanceId) valueAddedChange.getAddedValue()).getCdoId();
                                addedValueMapper(newlyAddedObjectFields, newAndRemovedObjectsMap.get(localCdoId.toString()));
                                listChangeFieldsDto.setListChangeFieldList(List.of(newlyAddedObjectFields));
                            }

                            if(listChanges instanceof ValueRemoved) {      //Removed Object
                                listChangeFieldsDto.setListElementChangeType("Removed Old Drug");
                                //refer above "Added New Drug" code
                            }

                            changeFieldsDtoList.add(listChangeFieldsDto);
                        });


        favoriteDrugChangeFieldMap.put("Favorite Drug:", changeFieldsDtoList);
        return favoriteDrugChangeFieldMap;
    }

    private void addedValueMapper(MyChangeFieldsDto myChangeFieldsDto, Object newlyAddedObject) {
        try {
            Class<?> clazz = newlyAddedObject.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // For accessing private fields

                String fieldName = field.getName();
                Object fieldValue = field.get(newlyAddedObject);

                myChangeFieldsDto.setFieldName(fieldName);
                myChangeFieldsDto.setFieldValue(String.valueOf(fieldValue));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //Output Resopnse

    /*
    *
    * [
    {
        "empId": "1",
        "changeFieldsList": [],
        "favoriteDrugsChangeFieldDto": {
            "Favorite Drug:": [
                {
                    "listElementChangeType": "Added New Drug",
                    "listChangeFieldList": [
                        {
                            "fieldName": "amount",
                            "fieldValue": "55.5"
                        }
                    ]
                }
            ]
        },
        "changeFieldsSize": 5
    },
    {
        "empId": "2",
        "changeFieldsList": [],
        "favoriteDrugsChangeFieldDto": {
            "Favorite Drug:": [
                {
                    "listElementChangeType": "Added New Drug",
                    "listChangeFieldList": [
                        {
                            "fieldName": "skills",
                            "fieldValue": "[agile coaching]"
                        }
                    ]
                }
            ]
        },
        "changeFieldsSize": 8
    }
]*/
}
