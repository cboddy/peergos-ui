
.PHONY: run_desktop
run_desktop: 
	./gradlew desktop:run

.PHONY: run_html5
run_html5:
	echo "Starting server @ http://localhost:8080/html/build/dist/"
	python -m SimpleHttpServer 8080

.PHONY: run_android
run_android:
	./gradlew android:installDebug android:run


.PHONY: deploy_desktop
deploy_desktop: 
	./gradlew desktop:dist

.PHONY: deploy_html5
#This is so resource intensive my machine grinds
#to a halt unless reduce the scheduling priorities.
deploy_html5: 
	nice -n 19 ./gradlew android:dist 

.PHONY: deploy_android
deploy_android:
	./gradlew android:assembleRelease
