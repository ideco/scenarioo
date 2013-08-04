'use strict';

NgUsdClientApp.directive('usdBreadcrumb', function($location, $route, $compile) {
    var limit = 25;
    var breadcrumbDescriptionObject = {
        restrict:'E',
        priority:0,
        replace:true,
        transclude:true,
        templateUrl:'template/breadcrumbs.html',
        scope: false,
        link:function (scope, element, attrs) {
            var ul = angular.element(element.children()[0]);
            var paths = splitPath($location.path());
            var path = "";
            var htmlCode = "";
            angular.forEach(paths, function (item, index) {
                item=item.trim();
                var last = index == (paths.length - 1);
                if (item=="" || (path[path.length-1]!='/')) {
                    path += '/';
                }
                path += encodeURIComponent(item);

                var routeKey = findPath($route.routes, path);
                var route = $route.routes[routeKey];
                if(route) {
                    var longText = decodeURIComponent(item);
                    var text = getShortenedText(longText);
                    var tooltip = getTooltip(longText);
                    if (route.breadcrumb != undefined) {
                        text = route.breadcrumb;
                        tooltip = "";
                    }
                    var href = "#" + getBreadCrumbPathWithParameters(path, routeKey);
                    var lnk;
                    if (!last) {
                        lnk = "<li " + tooltip + "><a href='" + href + "' " + tooltip + ">" + text + "</a> <span class='divider'>&gt;</span></li>"
                    } else {
                        if (attrs['name']) {
                            tooltip = "";
                            text = attrs['name'];
                        }
                        lnk = "<li class='active'  " + tooltip + ">" + text + "</li>";
                    }
                    htmlCode = htmlCode + lnk;
                }
            }, ul);
            element.html("<ul>"+htmlCode+"</ul>");
            $compile(element)(scope);
        }
    };
    return breadcrumbDescriptionObject;

    function getShortenedText(text) {
        if (text.length>limit) {
            var shortenedText = text.substr(0, limit);
            return shortenedText + "..";
        }
        return text;
    }

    function getTooltip(text) {
        if (text.length>limit) {
            return "tooltip='"+text+"' tooltip-placement='top'";
        }
        return "";
    }

    function findPath(routes, path) {
        for (var key in routes) {
            var routePath = replacePathParameters(key);
            routePath = removeFirstPartOfPath(routePath);
            if (path.match(new RegExp("^"+routePath+"$", "")) != null) {
                return key;
            }
        }
    }

    function replacePathParameters(routepath) {
        // Replace route parameters
        var routePathAsRegex = routepath.replace(new RegExp(":[^/]+", "gm"), "[^/]+");
        return routePathAsRegex;
    }

    function removeFirstPartOfPath(routepath) {
        // Replace first part of the routepath
        var routePathAsRegex = routepath.replace(new RegExp("^/[^/]+/", "gm"), "/[^/]+/");
        return routePathAsRegex;
    }

    function splitPath(path) {
        var parts = path.split('/');
        //Small fix to avoid problems with path "/"
        if (parts.length>1 && parts[0]==="" && parts[1]==="") {
            parts.splice(0, 1);
        }
        return parts;
    }

    function getBreadCrumbPathWithParameters(path, routeKey) {
        var parameters = splitPath(path);
        var parts = splitPath(routeKey);
        for (var i=0; i<parts.length; i++) {
            if (parts[i].match("^:")) {
                parts[i] = parameters[i];
            }
        }
        return parts.join("/");
    }
});