/**
 * Created by max on 15/07/14.
 */
angular.module('bikeManApp')
    .factory('dateSelectUtils', function() {

        function range(start, end) {
            var foo = [];
            for (var i = start; i <= end; i++) {
                foo.push(i);
            }
            return foo;
        }

        return {
            days: function () {
                return range(1,31);
            },
            months: function () {
                return [
                    { value: 0, name: 'January' },
                    { value: 1, name: 'February' },
                    { value: 2, name: 'March' },
                    { value: 3, name: 'April' },
                    { value: 4, name: 'May' },
                    { value: 5, name: 'June' },
                    { value: 6, name: 'July' },
                    { value: 7, name: 'August' },
                    { value: 8, name: 'September' },
                    { value: 9, name: 'October' },
                    { value: 10, name: 'November' },
                    { value: 11, name: 'December' }
                ];
            },
            years: function (amountOfYears) {
                now = parseInt(new Date().getFullYear());
                then = now - amountOfYears;
                return range(then,now);
            }
        }



    })
    .directive('dateSelect', ['dateSelectUtils', '$filter', function (dateSelectUtils, $filter) {
        return {
            restrict: 'A',
            replace: true,
            require: 'ngModel',
            scope: {
                model: '='
            },
            controller: ['$scope', 'dateSelectUtils', '$filter', function ($scope, dateSelectUtils, $filter) {

                $scope.days = dateSelectUtils.days();
                $scope.months = dateSelectUtils.months();
                $scope.years = dateSelectUtils.years(100);


                $scope.dateFields = {};
                $scope.$watch('model', function (newDate) {
                    $scope.dateFields.day = new Date(newDate).getUTCDate();
                    $scope.dateFields.month = new Date(newDate).getUTCMonth();
                    $scope.dateFields.year = new Date(newDate).getUTCFullYear();
                });

                $scope.formatDate = function (dateFields) {
                    var date = new Date(Date.UTC(dateFields.year, dateFields.month, dateFields.day));
                    return $filter('date')(date, "yyyy-MM-dd");
                }

                $scope.upDate = function () {
                    $scope.model = $scope.formatDate($scope.dateFields);
                }

                $scope.reset = function () {
                    $scope.dateFields.day = new Date().getUTCDate();
                    $scope.dateFields.month = new Date().getUTCMonth();
                    $scope.dateFields.year = new Date().getUTCFullYear();
                    $scope.model = $scope.formatDate($scope.dateFields);
                }

                // init
                $scope.reset();
            }],
            templateUrl: 'views/dateSelect.html'
           }
    }]);